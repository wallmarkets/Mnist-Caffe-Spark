import scala.math.Fractional
import scala.reflect.ClassTag
import org.bytedeco.javacpp.caffe.FloatBlob

class GenericNDArray[Dtype](val data: Array[Dtype], val shape: Array[Int], implicit val frac: Fractional[Dtype]) extends java.io.Serializable {
    assert(data.length == GenericNDArray.shapeSize(shape))
    import frac._

    def scalarDivide(v: Dtype) {
        for (i <- data.indices) 
            data(i) = data(i) / v
    }

    def plus(other: GenericNDArray[Dtype]) {
        assert(shape.deep == other.shape.deep)
        for (i <- data.indices) 
            data(i) = data(i) + other.data(i)
    }
}

object GenericNDArray {
    def apply[Dtype](shape: Array[Int])(implicit frac: Fractional[Dtype], tag: ClassTag[Dtype]) = {
        val data = Array.fill(shapeSize(shape))(frac.fromInt(0)) : Array[Dtype]
        new GenericNDArray[Dtype](data, shape, frac)
    }

    def apply[Dtype](data: Array[Dtype], shape: Array[Int])(implicit frac: Fractional[Dtype]) = {
        new GenericNDArray[Dtype](data, shape, frac)
    }

    def shapeSize(shape: Array[Int]) = shape.reduce(_ * _)

    def plus[Dtype](a: GenericNDArray[Dtype], b: GenericNDArray[Dtype])(implicit frac: Fractional[Dtype], tag: ClassTag[Dtype]) = {
        assert(a.shape.deep == b.shape.deep)
        val result = GenericNDArray[Dtype](a.shape)
        result.plus(a)
        result.plus(b)
        result
    }
}
