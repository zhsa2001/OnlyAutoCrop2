package imageProcessing

import java.awt.image.BufferedImage

/**
 * Регион обрезки изображения
 * @property x координата левого верхнего угла
 * @property y координата левого верхнего угла
 * @property w ширина вырезаемой области
 * @property x высота вырезаемой области
 * @property isSet были ли параметры установлены, неважно, вручную или авоматически
 * @property squareSize размер области для анализа при автоматическом определении обрезаемой рамки
 */
open class CropRegion {
    var x = 0
    var y = 0
    var w = 0
    var h = 0
    var isSet = false
    var squareSize = 30

    constructor()

    constructor(_x:Int,_y:Int,_w:Int,_h:Int){
        x = _x
        y = _y
        w = _w
        h = _h
        isSet = true
    }

    /**
     * Автоматически определяет все необходимые параметры по рамке изображения
     */
    open fun autoDetect(image: BufferedImage? = null){

    }
}