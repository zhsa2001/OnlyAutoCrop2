package imageProcessing

import java.awt.image.BufferedImage

open class CropRegion {
    var x = 0
    var y = 0
    var w = 0
    var h = 0
    var isSet = false
    var square_size = 30

    constructor()

    constructor(_x:Int,_y:Int,_w:Int,_h:Int){
        x = _x
        y = _y
        w = _w
        h = _h
        isSet = true
    }

    open fun autoDetect(image: BufferedImage? = null){

    }
}