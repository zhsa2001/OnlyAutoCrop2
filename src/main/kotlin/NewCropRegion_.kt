import java.awt.Point
import java.awt.image.BufferedImage

class NewCropRegion_: CropRegion() {
    override fun autoDetect(image: BufferedImage?) {
        if(image != null){
            var cornerLeft = Point()
            var cornerRight = Point()
            setRightCorner(image, cornerRight, square_size)
            setLeftDownCorner(image, cornerLeft, square_size)
            x = cornerLeft.x
            y = cornerRight.y
            w = cornerRight.x - x
            h = cornerLeft.y - y
            isSet = true
        }
    }
}