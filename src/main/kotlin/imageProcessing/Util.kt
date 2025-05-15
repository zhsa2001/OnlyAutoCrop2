package imageProcessing
import logic.FileAndDiapasons
import java.awt.Color
import java.awt.Image
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.Calendar
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

val threshold = 210

fun cropAndConcatManyImages(cropRegion: CropRegion,
                            eraseOnEachList: Boolean,
                            onUpdateProgress: (Int) -> Unit,
                            onCloseRequest: () -> Boolean,
                            onCurrentFileChanged: (String) -> Unit,
                            desiredHeight: Int = 0,
                            separateLists: Boolean = true,
                            time: Calendar = Calendar.Builder().setTimeOfDay(5,30,0).build()): String {
    var resMessage = ""
    var resFile: File? = null
    val imagesToConcatenate = mutableListOf<BufferedImage>()
    for(element in FileAndDiapasons.files){
        if(onCloseRequest()){
            break
        }
        try {
            onUpdateProgress(0)
            if(onCloseRequest()){
                break
            }
            element.file?.let {
                if(resFile == null){
                    resFile = File(it.parent + "/" + it.nameWithoutExtension + ".png")
//                    resFile = File(it.parent + "/" + it.nameWithoutExtension + "/")
                }
                onCurrentFileChanged(it.name)
                val images = ImageRepository().readAllImages(it)

                cropRegion.autoDetect(
                    BinaryColorSchemeConverter(threshold).convert(
                        GrayColorSchemeConverter().convert(images.last())))

                var imagesSize = images.size
                var progress = 15
                var start = 0
                var end = imagesSize
                onUpdateProgress(progress)
                element.start?.let {
                    start = max(min(imagesSize,element.start!!) - 1,0)
                    imagesSize -= start
                }
                element.end?.let {
                    end = min(max(element.end!!,start+1), images.size)
                    imagesSize -= (images.size - end)
                }
                var step = (100 - 20) / imagesSize

                val x = cropRegion.x
                val y = cropRegion.y

                val w = cropRegion.w
                val h = cropRegion.h
                val imagesCropped = mutableListOf<BufferedImage>()
                for(i in start..<end){
                    if(onCloseRequest()){
                        break
                    }
                    imagesCropped.add(images[i].getSubimage(x,y,w,h))
                    if (eraseOnEachList){
                        paintWhiteRightUpCorner(imagesCropped.last(), square_size = 40)
                    }
                }
                imagesToConcatenate.addAll(imagesCropped)
            }
        } catch (e: Exception){
            resMessage += "Ошибка обработки файла ${element.file!!.absolutePath}: ${e.message}\n"
        }
    }
    var fullWidth = 0
    var height =
        if (imagesToConcatenate.size > 0)
            if (desiredHeight > 0)
                desiredHeight
            else imagesToConcatenate[0].height
        else 0
    for(i in imagesToConcatenate.indices){
        if(onCloseRequest()){
            break
        }
        val scale = height.toDouble() / imagesToConcatenate[i].height
        val width = (imagesToConcatenate[i].width*scale).toInt()
        val im = BufferedImage(width,height,BufferedImage.TYPE_INT_RGB)
        val graphics = im.createGraphics()
        graphics.drawImage(imagesToConcatenate[i].getScaledInstance(width,height, Image.SCALE_SMOOTH),0,0,null)
        imagesToConcatenate[i] = im
        fullWidth += imagesToConcatenate[i].width
    }
    if(!onCloseRequest() && fullWidth > 0 && height > 0){
        if(separateLists){
            resFile?.let {
                resMessage += saveSeparately(resFile!!, time, imagesToConcatenate)
            }
        } else {
            resFile?.let {
                resMessage += saveFullImage(resFile!!, fullWidth, height, imagesToConcatenate)
            }
        }

    }

    return resMessage
}

fun getHourRoundUp(time: Calendar): Int{
    var time = time.clone() as Calendar
    time.add(Calendar.MINUTE, 59)
    return time.get(Calendar.HOUR_OF_DAY)
}

fun saveFullImage(resFile: File, fullWidth: Int, height: Int, images: MutableList<BufferedImage> ): String {
    var res = BufferedImage(fullWidth,height,BufferedImage.TYPE_INT_RGB)
    val resGraphics = res.createGraphics()
    var curWidth = 0
    for(im in images){
        resGraphics.drawImage(im,curWidth,0,null)
        curWidth += im.width
    }
    ImageIO.write(res,"PNG",resFile)
    return "Результат сохранен в ${resFile.absolutePath}\n"
}

fun saveSeparately(resFile: File, time: Calendar, images: MutableList<BufferedImage>): String{
    var folder = resFile!!.parent + "/" + resFile!!.nameWithoutExtension
    var number = 0
    while (Files.exists(Path(folder))){
        number++
        if(number == 1){
            folder += "($number)"
        } else {
            folder = folder.removeSuffix("(${number-1})")
            folder +="($number)"
        }
    }
    Files.createDirectory(Path(folder))
    var resFilePattern = "$folder/%s.png"
    var time = time.clone() as Calendar
    images.forEachIndexed { index, bufferedImage ->
        ImageIO.write(bufferedImage,"PNG",
            File(String.format(resFilePattern,
                (getHourRoundUp(time)).toString().padStart(2,'0') + '-' +
                        (getHourRoundUp(time) + 1).toString().padStart(2,'0')
            )));
        time.add(Calendar.MINUTE, 90)
    }
    return "Результат сохранен в ${folder}\n"
}

fun paintWhiteRightUpCorner(image: BufferedImage,square_size: Int) {
    val binary = BinaryColorSchemeConverter(200).convert(
        GrayColorSchemeConverter().convert(image)
    )
    for(i in square_size/2..<image.height-square_size){
        val line = findHorizontalLine(binary,image.width-square_size-1,i,square_size)
        if (line.size == 2 && abs(line[0].x - line[1].x) > square_size - 2){
            val draw = image.createGraphics()
            draw.color = Color.WHITE
            draw.fillRect(image.width - 60, 2,60,line[0].y - 2)
            break
        }
    }
}

fun checkT90(image: BufferedImage, x: Int, y: Int, squareSize: Int): Point? {
    var corner: Point? = null
    val verticalLine = findVerticalLine(image,x,y,squareSize)
    val horizontalLine = findHorizontalLine(image,x,y,squareSize)
    if(verticalLine.size == 2 && horizontalLine.size == 2
        && abs(verticalLine[0].y - verticalLine[1].y)>squareSize - 2
        && abs(verticalLine[1].x - horizontalLine[1].x)<4){
        corner = Point(horizontalLine[1].x,horizontalLine[1].y)
    }
    return corner
}

fun setRightCorner(image: BufferedImage, cornerRight: Point, square_size: Int){
    val raster = image.raster
    var pixel = IntArray(4)
    var find = false
    var x1 = 0
    var y1 = 0
    for (i in 0..<image.height-square_size/2){
        for (j in image.width-1 downTo square_size/2){
            raster.getPixel(j,i,pixel)
            if(pixel[0] == 0){

                val line = findVerticalLine(image,j - 2,i + square_size/2,square_size)

                if (line.size == 2 && abs(line[0].y - line[1].y) > (square_size - 2)){
                    find = true
                    x1  = line[0].x - square_size/2
                    y1 = line[0].y
                    break
                }
            }
        }
        if (find){
            break
        }
    }
    for(i in y1..<image.height-square_size){
        val corner = checkT90(image,x1,i,square_size)
        if(corner != null){
            cornerRight.x = corner.x
            cornerRight.y = corner.y
            break
        }
    }
}

fun setLeftDownCorner(image: BufferedImage, cornerLeft: Point, square_size: Int){
    val raster = image.raster
    var find = false
    var x1 = 0
    var y1 = 0
    var yStart = image.height-1
    while(!find && yStart > square_size){
        for(i in yStart downTo 0){
            val pixel = IntArray(4)
            raster.getPixel(image.width/2,i,pixel)
            if(pixel[0] == 0){
                val line = findHorizontalLine(image,image.width/2,i,square_size)
                if(line.size == 2 && abs(line[0].x - line[1].x) > square_size - 2){
                    y1 = i - square_size/2
                    x1 = image.width/2
                    break
                }
            }
        }
        for(j in x1 downTo 0){
            val corner = checkT180(image,j,y1,square_size)
            if(corner != null){
                cornerLeft.x = corner.x
                cornerLeft.y = corner.y
                find = true
            }
        }
        yStart = y1
    }
}

fun setLeftUpCorner(image: BufferedImage, cornerLeft: Point, cornerLeftBotom: Point, cornerRight: Point, square_size: Int){
    var find = false
    for(j in cornerLeftBotom.x+2..<image.width-square_size){
        for(i in cornerRight.y+2..<image.height-square_size) {

            var line = findVerticalLine(image,j,i,square_size)

            if(line.size == 2 && abs(line[0].y - line[1].y) > square_size - 2){
                cornerLeft.x = line[0].x
                cornerLeft.y = line[0].y
                find = true
            }
            if(find){
                break
            }

        }
        if(find){
            break
        }
    }
    return
}

fun checkT180(image: BufferedImage, x: Int, y: Int, squareSize: Int): Point? {
    var corner: Point? = null
    val verticalLine = findVerticalLine(image,x,y,squareSize)
    val horizontalLine = findHorizontalLine(image,x,y,squareSize)
    if(verticalLine.size == 2 && horizontalLine.size == 2
        && abs(horizontalLine[0].x - horizontalLine[1].x)>squareSize - 2
        && abs(verticalLine[1].y - horizontalLine[1].y)<4){
        corner = Point(verticalLine[1].x,verticalLine[1].y)
    }
    return corner
}

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

