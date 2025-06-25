package imageProcessing

import java.awt.Point
import java.awt.image.BufferedImage
import kotlin.math.max

/**
 * Наследник CropRegion для работы с файлами графиков из новой программы
 */
class NewCropRegion: CropRegion() {
    override fun autoDetect(image: BufferedImage?) {
        if(image != null){
            var cornerLeft = Point()
            var cornerRight = Point()
            setRightCorner(image, cornerRight, squareSize)
            setLeftDownCorner(image, cornerLeft, squareSize)
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