import java.io._
import java.nio.ByteBuffer

class Loader(path: String) {
  def getPath(filename: String) = path + '/' + filename
  val trainImages = Loader.getImages(getPath("train-images-idx3-ubyte"))
  val trainLabels = Loader.getLabels(getPath("train-labels-idx1-ubyte"))
  val testImages  = Loader.getImages(getPath("t10k-images-idx3-ubyte"))

}

object Loader {
  def getImages(filename: String): Array[Array[Float]] = {
    val stream = new FileInputStream(filename)

    val buf = new Array[Byte](4)
    stream.read(buf)
    assert(buf.deep == Array[Byte](0, 0, 8, 3).deep)
    
    stream.read(buf)
    // data is in big endian, and java likes it
    val numImages = ByteBuffer.wrap(buf).getInt()

    stream.read(buf)
    val imHeight = ByteBuffer.wrap(buf).getInt()
    assert(imHeight == 28)
    stream.read(buf)
    val imWidth  = ByteBuffer.wrap(buf).getInt()
    assert(imWidth  == 28)

    val images = new Array[Array[Float]](numImages)
    val imageBuffer = new Array[Byte](imWidth * imHeight)
    var i = 0
    for (i <- 0 until numImages) {
      stream.read(imageBuffer)
      images(i) = imageBuffer.map(e => ((e & 0xFF).toFloat / 255) - 0.5F)
    }
    images
  }

  def getLabels(filename: String): Array[Float] = {
    val stream = new FileInputStream(filename)

    val buf = new Array[Byte](4)
    stream.read(buf)
    assert(buf.deep == Array[Byte](0, 0, 8, 1).deep)

    stream.read(buf)
    val numLabels = ByteBuffer.wrap(buf).getInt()
    val labels = new Array[Byte](numLabels)
    stream.read(labels)
    labels.map(_.toFloat)
  }
}
