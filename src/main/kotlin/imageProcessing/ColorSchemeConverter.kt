package imageProcessing

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp

/**
 * Интерфейс для цветового преобразования изображения
 */
interface ColorSchemeConverter {
    /**
     * Преобразует цвета изображения
     * @param image исходное изображение
     * @return преобразованное изображение
     */
    fun convert(image: BufferedImage): BufferedImage
}

/**
 * Преобразователь цветных изображений в изображения в оттенках серого
 */
class GrayColorSchemeConverter: ColorSchemeConverter {
    override fun convert(image: BufferedImage): BufferedImage {
        val gray = BufferedImage(image.width,image.height, BufferedImage.TYPE_BYTE_GRAY)
        val xformOp = ColorConvertOp(null)
        xformOp.filter(image,gray)
        return gray
    }
}

/**
 * Преобразователь изображений в оттенках серого в черно-белые изображения
 */
class BinaryColorSchemeConverter(val threshold: Int): ColorSchemeConverter {
    override fun convert(image: BufferedImage): BufferedImage {
            val image = image
            val binaryRaster = image.getData()
            val pix = IntArray(4)
            for(i in 0..<image.width) {
                for (j in 0..<image.height) {
                    binaryRaster.getPixel(i, j, pix)
                    if (pix[0] > threshold) {
                        image.setRGB(i, j, Color.WHITE.getRGB())
                    } else {
                        image.setRGB(i, j, Color.BLACK.getRGB())
                    }
                }

            }
        return image
    }
}