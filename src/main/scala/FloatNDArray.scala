import scala.math.Fractional
import org.bytedeco.javacpp.caffe.FloatBlob

class FloatNDArray(val data: Array[Float], val shape: Array[Int]) extends java.io.Serializable {
    assert(data.length == FloatNDArray.shapeSize(shape))

    def scalarDivide(v: Float) {
        for (i <- data.indices) 
            data(i) = data(i) / v
    }

    def plus(other: FloatNDArray) {
        assert(shape.deep == other.shape.deep)
        for (i <- data.indices) 
            data(i) = data(i) + other.data(i)
    }
}

object FloatNDArray {
    def apply(shape: Array[Int]) = {
        val data = Array.fill(shapeSize(shape))(0) : Array[Float]
        new FloatNDArray(data, shape)
    }

    def apply(data: Array[Float], shape: Array[Int]) = {
        new FloatNDArray(data, shape)
    }

    def shapeSize(shape: Array[Int]) = shape.product

    def plus(a: FloatNDArray, b: FloatNDArray) = {
        assert(a.shape.deep == b.shape.deep)
        val result = FloatNDArray(a.shape)
        result.plus(a)
        result.plus(b)
        result
    }

    def getFloatBlobShape(blob: FloatBlob): Array[Int] = {
        val numAxes = blob.num_axes()
        val shape = new Array[Int](numAxes)
        for (k <- 0 to numAxes - 1) {
           shape(k) = blob.shape.get(k)
        }
        shape
    }

    def floatBlobToNDArray(blob: FloatBlob): FloatNDArray = {
        val shape = getFloatBlobShape(blob)
        val data = new Array[Float](shapeSize(shape))
        blob.cpu_data.get(data)
        FloatNDArray(data, shape)
    }
}
