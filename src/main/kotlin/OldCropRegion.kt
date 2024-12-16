import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max

class OldCropRegion: CropRegion() {
    override fun autoDetect(image: BufferedImage?) {
        if(image != null){
            var cornerLeft = Point()
            var cornerRight = Point()
            var cornerLeftUp = Point()
            setRightCorner(image, cornerRight, square_size)
            setLeftDownCorner(image, cornerLeft, square_size)
            var im2 = image
            var graph = im2.createGraphics()
            graph.color = Color.GRAY
            graph.drawRect(cornerLeft.x,cornerLeft.y,10,10)
            graph.drawRect(cornerRight.x,cornerRight.y,10,10)
            setLeftUpCorner(image, cornerLeftUp, cornerLeft,cornerRight,square_size*2)
            graph.drawRect(cornerLeftUp.x,cornerLeftUp.y,10,10)
            ImageIO.write(im2,"PNG", File("1.png"))
            x = cornerLeftUp.x
            y = cornerRight.y
            w = cornerRight.x - x + 1
            h = cornerLeft.y - y
            w = max(w,100)
            h = max(h,100)
            isSet = true
        }
    }
}