import scala.collection.mutable.Map
import scala.collection.mutable.MutableList

class CaffeWeightCollection(val col: Map[String, MutableList[FloatNDArray]])

object CaffeWeightCollection {
  
  def apply(): CaffeWeightCollection = {
    new CaffeWeightCollection(Map[String, MutableList[FloatNDArray]]())
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
    val newWeights = CaffeWeightCollection()
    for (name <- weights1.col.keys) {
      assert(weights1.col(name).length == weights2.col(name).length)
      val layer = weights1.col(name) zip weights2.col(name) map {
        case (a,b) => {
          assert(a.shape.deep == b.shape.deep)
          FloatNDArray.plus(a,b)
        }
      }
      newWeights.col += (name -> layer)
    }
    newWeights
  }
}
