package imageProcessing

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp

interface ColorSchemeConverter {
    abstract fun convert(image: BufferedImage): BufferedImage
}

class GrayColorSchemeConverter: ColorSchemeConverter {
    override fun convert(image: BufferedImage): BufferedImage {
        val gray = BufferedImage(image.width,image.height, BufferedImage.TYPE_BYTE_GRAY)
        val xformOp = ColorConvertOp(null)
        xformOp.filter(image,gray)
        return gray
    }
}

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