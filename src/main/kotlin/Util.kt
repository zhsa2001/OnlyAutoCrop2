
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import kotlin.math.abs

fun getFileFromChooseDialog(): File?{
    val fc = TifFileChooser()
    return if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        fc.selectedFile else null
}

fun cropAndConcat(images: MutableList<BufferedImage>, leftUp: Point, rightBottom: Point): BufferedImage {
    val width = rightBottom.x - leftUp.x
    val height = rightBottom.y - leftUp.y
    val resultImage = BufferedImage(width*images.size,height, BufferedImage.TYPE_INT_RGB)
    val processImage = resultImage.createGraphics()
    for(i in images.indices){
        processImage.drawImage(images[i].getSubimage(leftUp.x,leftUp.y,width,height), i*width,0,null)
//        processImage.color = Color.WHITE
//        processImage.fillRect((i+1)*width-50,10,50,40)
    }
    return resultImage
}

fun cropAndConcateImagesStartEnd(file1: File, file2:File,list1: Int,list2: Int, part1:Boolean, part2: Boolean, part3: Boolean,
                                 v: String, cropRegion: CropRegion,
                                 eraseHourOnEachList: Boolean,
                                 onUpdateProgress: (Int) -> Unit,
                                 onCloseRequest: () -> Boolean): String{
    var resMessage = ""
    var resFile: File?
        try {
            val images = ImageRepository().readAllImages(file1)
            val images2 = ImageRepository().readAllImages(file2)
            val imagesCropped = MutableList<BufferedImage?>(images.size){null}
            onUpdateProgress(5)
            var step = (100 - 20) / (images.size)
            cropRegion.autoDetect(
                BinaryColorSchemeConverter(180).convert(
                    GrayColorSchemeConverter().convert(images[0])))
//            println("$part1 $part2 $part3 $list1 $list2")
//            ImageIO.write(BinaryColorSchemeConverter(180).convert(
//                GrayColorSchemeConverter().convert(images[0])),"PNG",File("resFile.png"))
            var progress = 15
            onUpdateProgress(progress)
            val x = cropRegion.x
            val y = cropRegion.y

            val w = cropRegion.w
            val h = cropRegion.h
//            println("$x $y $w $h")
            val res = BufferedImage(w*images.size,h, BufferedImage.TYPE_INT_RGB)
            val resGraphics = res.createGraphics()
            for(i in images.indices){
                if(onCloseRequest()){
                    break
                }
                if(i < list1 - 1){
                    if(part1)
                        imagesCropped[i] = images[i].getSubimage(x,y,w,h)
                    else
                        imagesCropped[i] = images2[i].getSubimage(x,y,w,h)
                } else if(i < list2 - 1){
                    if(part2)
                        imagesCropped[i] = images[i].getSubimage(x,y,w,h)
                    else
                        imagesCropped[i] = images2[i].getSubimage(x,y,w,h)
                } else {

                        if(part3)
                            imagesCropped[i] = images[i].getSubimage(x,y,w,h)
                        else
                            imagesCropped[i] = images2[i].getSubimage(x,y,w,h)

                }

                if (eraseHourOnEachList) paintWhiteRightUpCorner(imagesCropped[i]!!, square_size = 40)

            }
            for(i in images.indices){
                if(onCloseRequest()){
                    break
                }
                resGraphics.drawImage(imagesCropped[i],i*w,0,null)
                progress += step
                onUpdateProgress(progress)
            }
            if(!onCloseRequest()){
                progress = 95
                onUpdateProgress(progress)
                resFile = File(file1.parent + "/" + file1.nameWithoutExtension + "_$v.png")
                ImageIO.write(res,"PNG",resFile)
                resMessage += "Результат сохранен в ${resFile.absolutePath}\n"
            }
        } catch (e: Exception){
            resMessage += "Ошибка обработки файла: ${e.message}\n"
        }

    return resMessage
}


fun cropAndConcateImages(files: List<File>,
                         cropRegion: CropRegion,
                         onUpdateProgress: (Int) -> Unit,
                         onCloseRequest: () -> Boolean,
                         onCurrentFileChanged: (String) -> Unit,
): String {
    var resMessage = ""
    var resFile: File?
    for(file in files){
        try {
            if(onCloseRequest()){
                break
            }
            onCurrentFileChanged(file.name)
            val images = ImageRepository().readAllImages(file)
            val imagesCropped = MutableList<BufferedImage?>(images.size){null}
            onUpdateProgress(5)
            var step = (100 - 20) / (images.size)
            cropRegion.autoDetect(
                BinaryColorSchemeConverter(180).convert(
                    GrayColorSchemeConverter().convert(images[0])))

            var progress = 15
            onUpdateProgress(progress)
            val x = cropRegion.x
            val y = cropRegion.y

            val w = cropRegion.w
            val h = cropRegion.h
            val res = BufferedImage(w*images.size,h, BufferedImage.TYPE_INT_RGB)
            val resGraphics = res.createGraphics()
            for(i in images.indices){
                if(onCloseRequest()){
                    break
                }
                imagesCropped[i] = images[i].getSubimage(x,y,w,h)
                paintWhiteRightUpCorner(imagesCropped[i]!!, square_size = 40)

            }
            for(i in images.indices){
                if(onCloseRequest()){
                    break
                }
                resGraphics.drawImage(imagesCropped[i],i*w,0,null)
                progress += step
                onUpdateProgress(progress)
            }
            if(!onCloseRequest()){
                progress = 95
                onUpdateProgress(progress)
                resFile = File(file.parent + "/" + file.nameWithoutExtension + ".png")
                ImageIO.write(res,"PNG",resFile)
                resMessage += "Результат сохранен в ${resFile.absolutePath}\n"
            }
        } catch (e: Exception){
            resMessage += "Ошибка обработки файла ${file.absolutePath}: ${e.message}\n"
        }
    }
    return resMessage
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


fun setCorners(image: BufferedImage, cornerLeft: Point, cornerRight: Point, square_size: Int) {
    setRightCorner(image, cornerRight, square_size)
    setLeftDownCorner(image, cornerLeft, square_size)
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
                val line = findVerticalLine(image,j - square_size/2,i + square_size/2,square_size)
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
    for(i in image.height-1 downTo 0){
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
            break
        }
    }
}


fun setLeftUpCorner(image: BufferedImage, cornerLeft: Point, cornerLeftBotom: Point, cornerRight: Point, square_size: Int){
    val raster = image.raster
    var find = false

    var count = 0
    val pixel = IntArray(4)
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


fun checkIsNum(num: String):Boolean{
    var res = true
    for(c in num){
        if(!c.isDigit()){
            res = false
            break
        }
    }
    return res
}

