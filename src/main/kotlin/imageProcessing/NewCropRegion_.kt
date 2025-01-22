package imageProcessing

import java.awt.Point
import java.awt.image.BufferedImage
import kotlin.math.max

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
            w = max(w,100)
            h = max(h,100)
            isSet = true
        }
    }
}