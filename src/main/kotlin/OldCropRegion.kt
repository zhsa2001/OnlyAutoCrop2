import java.awt.Point
import java.awt.image.BufferedImage

class OldCropRegion: CropRegion() {
    override fun autoDetect(image: BufferedImage?) {
        if(image != null){
            var cornerLeft = Point()
            var cornerRight = Point()
            var cornerLeftUp = Point()
            setRightCorner(image, cornerRight, square_size)
            setLeftDownCorner(image, cornerLeft, square_size)
            setLeftUpCorner(image, cornerLeftUp, cornerLeft,cornerRight,square_size)
            x = cornerLeftUp.x
            y = cornerRight.y
            w = cornerRight.x - x + 1
            h = cornerLeft.y - y
            isSet = true
        }
    }
}