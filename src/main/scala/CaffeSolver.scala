import org.bytedeco.javacpp.caffe._
import org.bytedeco.javacpp.FloatPointer

class CaffeSolver(solverParam: SolverParameter) {
  val caffeSolver = FloatSolverRegistry.CreateSolver(solverParam)
  val caffeNet = caffeSolver.net()
  val numOutputs = caffeNet.num_outputs
  val numLayers = caffeNet.layers().size.toInt
  val layerNames = List.range(0, numLayers).map(i => caffeNet.layers.get(i).layer_param.name.getString)
  val numLayerBlobs = List.range(0, numLayers).map(i => caffeNet.layers.get(i).blobs().size.toInt)
  var dataPointer = None : Option[FloatPointer]
  var lablPointer = None : Option[FloatPointer]

  def Forward() {
    caffeNet.Forward()
  }

  def Step(n: Int) {
    caffeSolver.Step(n)
  }

  def setData(data: FloatPointer, labl: FloatPointer, n: Int) {
    if (!dataPointer.isDefined || !dataPointer.get.equals(data) || 
        !lablPointer.isDefined || !lablPointer.get.equals(labl)) {
      val memorydata = caffeNet.layer_by_name(classOf[FloatMemoryDataLayer], "mnist")
      val batchSize  = memorydata.batch_size()
      memorydata.Reset(data, labl, n / batchSize * batchSize)
      dataPointer = Some(data)
      lablPointer = Some(labl)
    }
  }

  def getBlobs(dataBlobNames: List[String] = List[String]()): Map[String, FloatNDArray] = {
    val outputs = Map[String, FloatNDArray]()
    for (name <- dataBlobNames) {
      val floatBlob = caffeNet.blob_by_name(name)
      if (floatBlob == null) {
        throw new IllegalArgumentException("The net does not have a layer named " + name + ".\n")
      }
      outputs += (name -> FloatNDArray.floatBlobToNDArray(floatBlob))
    }
    return outputs
  }

  def getWeights(): CaffeWeightCollection = {
    val weights = 
      (0 until numLayers) { case i =>
        val blobs = caffeNet.layers().get(i).blobs()
        val weightList = 
          (0 until numLayerBlobs(i)) map { case j =>
            val blob = blobs.get(j)
            val shape = FloatNDArray.getFloatBlobShape(blob)
            val data = new Array[Float](shape.product)
            blob.cpu_data.get(data, 0, data.length)
            FloatNDArray(data, shape)
          }
        (layerNames(i), weightList)
      }
    CaffeWeightCollection(weights)
  }

  def setWeights(weights: CaffeWeightCollection) = {
    assert(weights.col.keys.size == numLayers)
    for (i <- 0 to numLayers - 1) {
      val blobs = caffeNet.layers().get(i).blobs()
      val layer = weights.col(layerNames(i))
      for (j <- 0 to numLayerBlobs(i) - 1) {
        val source = layer(j)
        // var shape = FloatNDArray.getFloatBlobShape(blob).deep
        // assert(shape == source.shape.deep) // check that weights are the correct shape
        val flatWeights = source.asFloat
        blobs.get(j).mutable_cpu_data.put(flatWeights, 0, flatWeights.length)
      }
    }
  }
}
