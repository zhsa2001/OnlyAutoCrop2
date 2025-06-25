package imageProcessing

import java.awt.Point
import java.awt.image.BufferedImage
import kotlin.math.max

/**
 * Наследник CropRegion для работы с файлами графиков из старой программы
 */
class OldCropRegion: CropRegion() {
    override fun autoDetect(image: BufferedImage?) {
        if(image != null){
            val cornerLeft = Point()
            val cornerRight = Point()
            val cornerLeftUp = Point()
            setRightCorner(image, cornerRight, squareSize)
            setLeftDownCorner(image, cornerLeft, squareSize)
            setLeftUpCorner(image, cornerLeftUp, cornerLeft, cornerRight, squareSize * 2)
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