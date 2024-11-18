import java.awt.Point
import java.awt.image.BufferedImage

fun findHorizontalLine(image: BufferedImage,x: Int,y: Int,square_size: Int): List<Point>{
    var lineStart: Point? = null
    var lineEnd: Point? = null
    val raster = image.raster
    var pixel = IntArray(4)
    for(i in 0..<square_size){
        raster.getPixel(x,y+i,pixel)
        if(pixel[0] == 0){
            lineStart = Point(x,y+i)
            for(j in 0..<square_size){
                raster.getPixel(x+j,y+i,pixel)
                if(pixel[0] != 0){
                    lineEnd = Point(x+j-1,y+i)
                    break
                }
            }
            if(lineEnd == null){
                lineEnd = Point(x+square_size-1,y+i)
            }
        }
        if(lineEnd != null){
            break
        }
    }
    val line = mutableListOf<Point>()
    if(lineEnd != null){
        line.add(lineStart!!)
        line.add(lineEnd!!)
    }
    return line
}

fun findVerticalLine(image: BufferedImage,x: Int,y: Int,square_size: Int): List<Point>{
    var lineStart: Point? = null
    var lineEnd: Point? = null
    val raster = image.raster
    var pixel = IntArray(4)
    for(j in 0..<square_size){
        raster.getPixel(x+j,y,pixel)
        if(pixel[0] == 0){
            lineStart = Point(x+j,y)
            for(i in 0..<square_size){
                raster.getPixel(x+j,y+i,pixel)
                if(pixel[0] != 0){
                    lineEnd = Point(x+j,y+i-1)
                    break
                }
            }
            if(lineEnd == null){
                lineEnd = Point(x+j,y+square_size-1)
            }
        }
        if(lineEnd != null){
            break
        }
    }
    val line = mutableListOf<Point>()
    if(lineEnd != null){
        line.add(lineStart!!)
        line.add(lineEnd!!)
    }
    return line
}
