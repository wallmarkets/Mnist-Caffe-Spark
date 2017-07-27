import org.bytedeco.javacpp.caffe._
import org.bytedeco.javacpp.FloatPointer

import scala.collection.mutable.Map
import scala.collection.mutable.MutableList

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

  def getWeights(): Map[String, MutableList[FloatNDArray]] = {
    val weights = Map[String, MutableList[FloatNDArray]]()
    for (i <- 0 to numLayers - 1) {
      val weightList = MutableList[FloatNDArray]()
      for (j <- 0 to numLayerBlobs(i) - 1) {
        val blob = caffeNet.layers().get(i).blobs().get(j)
        val shape = FloatNDArray.getFloatBlobShape(blob)
        val data = new Array[Float](shape.product)
        blob.cpu_data.get(data, 0, data.length)
        weightList += FloatNDArray(data, shape)
      }
      weights += (layerNames(i) -> weightList)
    }
    return weights
  }

  def setWeights(weights: Map[String, MutableList[FloatNDArray]]) = {
    assert(weights.keys.size == numLayers)
    for (i <- 0 to numLayers - 1) {
      for (j <- 0 to numLayerBlobs(i) - 1) {
        val blob = caffeNet.layers().get(i).blobs().get(j)
        val source = weights(layerNames(i))(j)
        // var shape = FloatNDArray.getFloatBlobShape(blob).deep
        // assert(shape == source.shape.deep) // check that weights are the correct shape
        val flatWeights = source.asFloat
        blob.mutable_cpu_data.put(flatWeights, 0, flatWeights.length)
      }
    }
  }
}
