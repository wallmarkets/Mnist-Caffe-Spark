// import scala.math.Fractional
import org.bytedeco.javacpp.caffe.FloatBlob
import org.nd4s.Implicits._
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class FloatNDArray(val data: INDArray) extends java.io.Serializable {

    def scalarDivide(v: Float) {
        data /= v
    }

    def plus(other: FloatNDArray) {
        data += other.data
    }

    def shape: Array[Int] = data.shape()

    def asFloat: Array[Float] = data.data().asFloat()
}

object FloatNDArray {
    def apply(shape: Array[Int]) = {
        new FloatNDArray(Nd4j.zeros(shape : _*))
    }

    def apply(data: Array[Float], shape: Array[Int]) = {
        new FloatNDArray(Nd4j.create(data, shape))
    }

    def plus(a: FloatNDArray, b: FloatNDArray) = {
        new FloatNDArray(a.data + b.data)
    }

    def getFloatBlobShape(blob: FloatBlob): Array[Int] = {
        val numAxes = blob.num_axes()
        numAxes match {
            case 0 => Array(1)
            case 1 => Array(1, blob.shape.get(0))
            case n => {
                val shape = new Array[Int](numAxes)
                for (k <- 0 to numAxes - 1) {
                    shape(k) = blob.shape.get(k)
                }
                shape                
            }
        }
    }

    def floatBlobToNDArray(blob: FloatBlob): FloatNDArray = {
        val shape = getFloatBlobShape(blob)
        val data = new Array[Float](shape.product)
        blob.cpu_data.get(data)
        FloatNDArray(data, shape)
    }
}
