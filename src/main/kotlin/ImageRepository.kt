import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageRepository {
//    val images = mutableListOf<BufferedImage>()
    fun readAllImages(file: File):MutableList<BufferedImage>{
        val stream = ImageIO.createImageInputStream(file)
        val images = mutableListOf<BufferedImage>()
        val imageReaders = ImageIO.getImageReaders(stream)
        val reader = imageReaders.next()
        reader.setInput(stream)
        var i = 0
        var image: BufferedImage
        val count = reader.getNumImages(true)
        while(count>i){
            image = reader.read(i++)
            images.add(image)
        }
        reader.dispose()
        return images
    }
//    companion object {
//
//    }
}