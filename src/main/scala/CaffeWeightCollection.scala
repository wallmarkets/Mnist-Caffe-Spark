import scala.collection.Map

class CaffeWeightCollection(private val col: Map[String, Array[FloatNDArray]]) extends Serializable {
  def layer(name: String): Array[FloatNDArray] = {
    col(name)
  }
  def numLayers = col.keys.size
}

object CaffeWeightCollection {
  
  def apply(): CaffeWeightCollection = {
    new CaffeWeightCollection(Map[String, Array[FloatNDArray]]())
  }

  def apply(seq: Iterable[(String, Array[FloatNDArray])]): CaffeWeightCollection = {
    new CaffeWeightCollection(seq.toMap)
  }

  def scalarDivide(weights: CaffeWeightCollection, v: Float): Unit = {
    for (layer <- weights.col.values) {
      for (param <- layer) {
        param.scalarDivide(v)
      }
    }
  }

  def add(weights1: CaffeWeightCollection, weights2: CaffeWeightCollection): CaffeWeightCollection = {
    assert(weights1.col.keys == weights2.col.keys)
    val newWeights = 
      weights1.col.keys map {
        case name => 
          assert(weights1.col(name).length == weights2.col(name).length)
          val layer = weights1.col(name) zip weights2.col(name) map {
            case (a,b) => {
              assert(a.shape.deep == b.shape.deep)
              FloatNDArray.plus(a,b)
            }
          }
          (name, layer)
      }
    CaffeWeightCollection(newWeights)
  }
}
