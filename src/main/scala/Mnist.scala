import org.apache.spark.sql.types._
import org.apache.spark.sql.SparkSession
import org.bytedeco.javacpp.caffe._
import org.bytedeco.javacpp.FloatPointer

class Solver(val home: String) extends Serializable {
  // keep netParam and solverParam as a member here, so that they won't be GC'ed.
  lazy val netParam = new NetParameter()
  lazy val solverParam = new SolverParameter()
  lazy val instance = {
    ReadProtoFromTextFileOrDie(home + "/model/mnist_net.prototxt", netParam)
    ReadSolverParamsFromTextFileOrDie(home + "/model/mnist_solver.prototxt", solverParam)
    solverParam.clear_net()
    solverParam.set_allocated_net_param(netParam)
    Caffe.set_mode(Caffe.CPU)
    val solver = new CaffeSolver(solverParam)
    solver
  }
}

object MnistApp {
  val trainBatchSize = 64
  val testBatchSize  = 64

  def main(args: Array[String]) {

    val builder = SparkSession
      .builder()
      .appName("Mnist")

    val mnistHome   = sys.env("MNIST_HOME")

    val numWorkers = args(0).toInt
  
    val spark = SparkSession.builder.getOrCreate()
    import spark.implicits._
    val sc = spark.sparkContext
    val logger = new Logger(mnistHome + "/training_log_" + System.currentTimeMillis().toString + ".txt")

    val loader = new Loader(mnistHome + "/model")
    logger.log("loading train data")
    var trainRDD = sc.parallelize(loader.trainImages.zip(loader.trainLabels))
    logger.log("loading test data")
    var testRDD = sc.parallelize(loader.testImages.zip(loader.testLabels))

    logger.log("repartition data")
    trainRDD = trainRDD.repartition(numWorkers)
    testRDD = testRDD.repartition(numWorkers)

    val numTrainData = trainRDD.count()
    logger.log("numTrainData = " + numTrainData.toString)

    val numTestData = testRDD.count()
    logger.log("numTestData = " + numTestData.toString)

    val workers = sc.parallelize(Array.fill(numWorkers)(new Solver(mnistHome)), numWorkers).cache()
    var trainPartitionSizes = trainRDD.mapPartitions(iter => Iterator.single(iter.size), true).cache()
    var trainPartitionMem   = trainRDD.mapPartitions(iter => Iterator.single(makeFloatPointer(iter)), true).cache()
    val testPartitionSizes  = testRDD .mapPartitions(iter => Iterator.single(iter.size), true).cache()
    val testParititonMem    = testRDD .mapPartitions(iter => Iterator.single(makeFloatPointer(iter)), true).cache()
    logger.log("trainPartitionSizes = " + trainPartitionSizes.collect().deep.toString)
    logger.log("testPartitionSizes  = " + testPartitionSizes.collect().deep.toString)

    logger.log("start obtaining initial net weights.")

    // initialize weights on master
    var netWeights = workers.map(caffeSolver => caffeSolver.instance.getWeights()).collect()(0)

    logger.log("inital net weights obtained.")

    var i = 0
    while (true) {
      logger.log("broadcasting weights", i)
      val broadcastWeights = sc.broadcast(netWeights)
      logger.log("setting weights on workers", i)
      workers.foreach(caffeSolver => caffeSolver.instance.setWeights(broadcastWeights.value))

      if (i % 5 == 0) {
        logger.log("testing", i)
        val testAccuracies = workers.zipPartitions(testPartitionSizes, testParititonMem) {
          case (svIt, szIt, dtIt) => 
            val caffeSolver = svIt.next
            val (data, labels) = dtIt.next
            val size = szIt.next
            var accuracy = 0F
            caffeSolver.instance.setData(data, labels, size)
            for (_ <- 1 to size / testBatchSize) {
              caffeSolver.instance.net().ForwardPrefilled()
            }
            val out = caffeSolver.instance.getBlobs(List("accuracy", "loss", "prob"))
            accuracy += out("accuracy").data(0)
            Iterator.single(accuracy)
        }.collect()
        logger.log(s"accuracy: ${testAccuracies.deep}", i)
        val accuracy = testAccuracies.sum / testAccuracies.length
        logger.log("%.2f".format(100F * accuracy) + "% accuracy", i)

        // logger.log("shuffle the data...", i)
        // trainRDD = trainRDD.repartition(numWorkers)
        // trainPartitionSizes = trainRDD.mapPartitions(iter => Iterator.single(iter.size), true)
        // trainPartitionMem   = trainRDD.mapPartitions(iter => Iterator.single(makeFloatPointer(iter)), true)
      }

      logger.log("training", i)
      workers.zipPartitions(trainPartitionSizes, trainPartitionMem) {
        case (svIt, szIt, dtIt) => 
          val caffeSolver = svIt.next
          val (data, labels) = dtIt.next
          val size = szIt.next
          val t1 = System.currentTimeMillis()
          caffeSolver.instance.setData(data, labels, size)
          caffeSolver.instance.Step(size / trainBatchSize)
          val t2 = System.currentTimeMillis()
          print(s"iters took ${((t2 - t1) * 1F / 1000F).toString}s, # batches ${size / trainBatchSize}\n")
          Iterator.single(())
      }.count()
      logger.log("collecting weights", i)
      netWeights = workers.map(caffeSolver => { caffeSolver.instance.getWeights() }).reduce((a, b) => CaffeWeightCollection.add(a, b))
      CaffeWeightCollection.scalarDivide(netWeights, 1F * numWorkers)
      logger.log("weight = " + netWeights("conv1")(0).data(0).toString, i)
      i += 1
    }

    logger.log("finished training")
  }

  // the java wrapper of MemoryDataLayer::Reset provides two alternatives, Array[Float] or 
  // java.nio.FloatBuffer, however neither works because of GC.
  // 
  // We need a piece of pinned storage.
  // 
  def makeFloatPointer(iter: Iterator[(Array[Float], Float)]): (FloatPointer, FloatPointer) = {
    val (data, labl) = iter.toArray.unzip
    val dataflatten = data.flatten
    val datanativ   = new FloatPointer(dataflatten :_*)
    val lablnativ   = new FloatPointer(labl :_*)
    (datanativ, lablnativ)
  }
}