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

/**
 * Граничное значение для бинаризации изображения
 */
val threshold = 210

/**
 * Вырезает определенные листы из файлов FileAndDiapasons и сохраняет изображение-результат
 * @param cropRegion регион для обрезки файла
 * @param eraseOnEachList закрашивать или нет верхний правый угол (где дублируется час)
 * @param onCloseRequest для обновления процента выполнения по текущему файлу
 * @param onCurrentFileChanged для обновления названия обрабатываемого файла
 * @param desiredHeight высота итогового изображения. Если 0, то высота не изменяется
 * @param separateLists сохранить каждый лист отдельно или объединить в одно изображение
 * @param time время на первом листе
 * @return строку с сообщением о результате обработки файлов
 */
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

                // определяется диапазон листов
                element.start?.let {
                    start = max(min(imagesSize,element.start!!) - 1,0)
                    imagesSize -= start
                }
                element.end?.let {
                    end = min(max(element.end!!,start+1), images.size)
                    imagesSize -= (images.size - end)
                }
                var step = (100 - progress) / imagesSize

                val x = cropRegion.x
                val y = cropRegion.y

                val w = cropRegion.w
                val h = cropRegion.h
                val imagesCropped = mutableListOf<BufferedImage>()
                // заполняется список с обрезанными изображениями
                for(i in start..<end){
                    if(onCloseRequest()){
                        break
                    }
                    imagesCropped.add(images[i].getSubimage(x,y,w,h))
                    if (eraseOnEachList){
                        paintWhiteRightUpCorner(imagesCropped.last(), squareSize = 40)
                    }
                    progress += step
                    onUpdateProgress(progress)
                }
                imagesToConcatenate.addAll(imagesCropped)
            }
        } catch (e: Exception){
            resMessage += "Ошибка обработки файла ${element.file!!.absolutePath}: ${e.message}\n"
        }
    }

    var height =
        if (imagesToConcatenate.size > 0)
            if (desiredHeight > 0)
                desiredHeight
            else imagesToConcatenate[0].height
        else 0
    onCurrentFileChanged("итоговый файл")
    onUpdateProgress(0)
    var fullWidth = 0
    // изображения масштабируются по высоте
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

        onUpdateProgress(100 * (i + 1) / imagesToConcatenate.size)
    }
    if(!onCloseRequest() && fullWidth > 0 && height > 0){
        // изображения сохраняются
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

/**
 * Округляет час
 * @param time время для округления
 * @return округленный до большего час
 */
fun getHourRoundUp(time: Calendar): Int{
    val time = time.clone() as Calendar
    time.add(Calendar.MINUTE, 59)
    return time.get(Calendar.HOUR_OF_DAY)
}

/**
 * Сохраняет объединенным изображением список листов графика
 * @param resFile файл, по названию которого определяется название сохраняемого файла
 * @param fullWidth ширина итогового изображения
 * @param height высота итогового изображения
 * @param images список изображений-листов для сохранения
 * @return строку с информацией о том, куда сохранен файл
 */
fun saveFullImage(resFile: File, fullWidth: Int, height: Int, images: MutableList<BufferedImage> ): String {
    var res = BufferedImage(fullWidth,height,BufferedImage.TYPE_INT_RGB)
    val resGraphics = res.createGraphics()
    var curWidth = 0
    // отрисовка каждого изображения на итоговом
    for(im in images){
        resGraphics.drawImage(im,curWidth,0,null)
        curWidth += im.width
    }
    ImageIO.write(res,"PNG",resFile)
    return "Результат сохранен в ${resFile.absolutePath}\n"
}

/**
 * Сохраняет отдельными файлами список листов графика
 * @param resFile файл, по названию которого создается новая директория для сохранения файлов
 * @param time время на первом листе для именования файлов-листов
 * @param images список изображений-листов для сохранения
 * @return строку с информацией о том, куда сохранены файлы
 */
fun saveSeparately(resFile: File, time: Calendar, images: MutableList<BufferedImage>): String{
    // название папки для сохранения файлов
    var folder = resFile.parent + "/" + resFile.nameWithoutExtension
    var number = 0
    // определение еще не существующей папки с таким названием и индексом для сохранения
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
    // сохранение листов с диапазоном часов в названии документа
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

/**
 * Закрашивает белым угол справа сверху
 * @param image изменяемое изображение
 * @param squareSize сторона квадрата для анализа области
 * @param widthOfFilledRect ширина закрашиваемой области
 */
fun paintWhiteRightUpCorner(image: BufferedImage, squareSize: Int, widthOfFilledRect: Int = 60) {
    val binary = BinaryColorSchemeConverter(threshold).convert(
        GrayColorSchemeConverter().convert(image)
    )
    // определение высоты закрашиваемой области по горизонтальной линии минут
    for(i in squareSize/2..<image.height-squareSize){
        val line = findHorizontalLine(binary,image.width-squareSize-1,i,squareSize)
        if (line.size == 2 && abs(line[0].x - line[1].x) > squareSize - 2){
            val draw = image.createGraphics()
            draw.color = Color.WHITE
            draw.fillRect(image.width - widthOfFilledRect, 2,widthOfFilledRect,line[0].y - 2)
            break
        }
    }
}

/**
 * Находит в области угол в виде повернутой по часовой на 90 градусов буквы Т
 * @param image черно-белое изображение для анализа
 * @param x координата верхнего левого угла области
 * @param y координата верхнего левого угла области
 * @param squareSize сторона квадратной области для анализа
 * @return координату угла, если он найден, иначе null
 */
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

/**
 * Устанавливает черно-белое правый верхний внутренний угол рамки листов графика
 * @param image изображение для анализа
 * @param cornerRight устанавливаемый угол
 * @param squareSize сторона квадратной области для анализа
 */
fun setRightCorner(image: BufferedImage, cornerRight: Point, squareSize: Int){
    val raster = image.raster
    var pixel = IntArray(4)
    var find = false
    var x1 = 0
    var y1 = 0
    for (i in 0..<image.height-squareSize/2){
        // обход справа налево сверху вниз для нахождения вертикальной линии
        for (j in image.width-1 downTo squareSize/2){
            raster.getPixel(j,i,pixel)
            if(pixel[0] == 0){
                val line = findVerticalLine(image,j - 2,i + squareSize/2,squareSize)
                if (line.size == 2 && abs(line[0].y - line[1].y) > (squareSize - 2)){
                    find = true
                    x1  = line[0].x - squareSize/2
                    y1 = line[0].y
                    break
                }
            }
        }
        if (find){
            break
        }
    }
    for(i in y1..<image.height-squareSize){
        // обход сверху вниз для определения угла в виде Т, повернутой на 90 градусов по часовой
        val corner = checkT90(image,x1,i,squareSize)
        if(corner != null){
            cornerRight.x = corner.x
            cornerRight.y = corner.y
            break
        }
    }
}

/**
 * Устанавливает левый нижний внутренний угол рамки листов графика
 * @param image черно-белое изображение для анализа
 * @param cornerLeft устанавливаемый угол
 * @param squareSize сторона квадратной области для анализа
 */
fun setLeftDownCorner(image: BufferedImage, cornerLeft: Point, squareSize: Int){
    val raster = image.raster
    var find = false
    var x1 = 0
    var y1 = 0
    var yStart = image.height-1

    while(!find && yStart > squareSize){
        // обход снизу вверх для нахождения нижней горизонтальной линии
        for(i in yStart downTo 0){
            val pixel = IntArray(4)
            raster.getPixel(image.width/2,i,pixel)
            if(pixel[0] == 0){
                val line = findHorizontalLine(image,image.width/2,i,squareSize)
                if(line.size == 2 && abs(line[0].x - line[1].x) > squareSize - 2){
                    y1 = i - squareSize/2
                    x1 = image.width/2
                    break
                }
            }
        }
        // обход справа налево для нахождения внутреннего левого нижнего угла рамки
        for(j in x1 downTo 0){
            val corner = checkT180(image,j,y1,squareSize)
            if(corner != null){
                cornerLeft.x = corner.x
                cornerLeft.y = corner.y
                find = true
            }
        }
        yStart = y1
    }
}

/**
 * Устанавливает левый верхний внутренний угол рамки листов графика, учитывая отступ, имеющийся на графиках из старой программы
 * @param image черно-белое изображение для анализа
 * @param cornerLeft устанавливаемый угол
 * @param cornerLeftBotom нижний левый внутренний угол рамки
 * @param cornerRight правый верхний внутренний угол рамки
 * @param squareSize сторона квадратной области для анализа
 */
fun setLeftUpCorner(image: BufferedImage, cornerLeft: Point, cornerLeftBotom: Point, cornerRight: Point, squareSize: Int){
    var find = false
    for(j in cornerLeftBotom.x+2..<image.width-squareSize){
        for(i in cornerRight.y+2..<image.height-squareSize) {
            // обход снизу вверх слева направо для нахождения верикальной линии
            val line = findVerticalLine(image,j,i,squareSize)

            if(line.size == 2 && abs(line[0].y - line[1].y) > squareSize - 2){
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

/**
 * Определяет в квадратной области черно-белого изображения координаты угла, который выглядит, как перевернутая на 180 градусов Т
 * @param image изображение для анализа
 * @param x координата верхнего угла квадратной области BufferedImage
 * @param y координата верхнего угла квадратной области BufferedImage
 * @param squareSize сторона квадратной области для анализа
 * @return координату угла, если он найден, иначе null
 */
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

/**
 * Определяет в квадратной области черно-белого изображения горизонтальную линию, начинающуюся на ширине x,
 * обходя слева направо, сверху вниз
 * @param image изображение для анализа
 * @param x координата верхнего угла квадратной области BufferedImage
 * @param y координата верхнего угла квадратной области BufferedImage
 * @param squareSize сторона квадратной области для анализа
 * @return массив из двух точек или пустой массив, если линия не найдена
 */
fun findHorizontalLine(image: BufferedImage, x: Int, y: Int, squareSize: Int): List<Point>{
    var lineStart: Point? = null
    var lineEnd: Point? = null
    val raster = image.raster
    var pixel = IntArray(4)
    for(i in 0..<squareSize){
        raster.getPixel(x,y+i,pixel)
        if(pixel[0] == 0){
            lineStart = Point(x,y+i)
            for(j in 0..<squareSize){
                raster.getPixel(x+j,y+i,pixel)
                if(pixel[0] != 0){
                    lineEnd = Point(x+j-1,y+i)
                    break
                }
            }
            if(lineEnd == null){
                lineEnd = Point(x+squareSize-1,y+i)
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

/**
 * Определяет в квадратной области черно-белого изображения вертикальную линию, начинающуюся на высоте y,
 * обходя слева направо, сверху вниз
 * @param image изображение для анализа
 * @param x координата верхнего угла квадратной области BufferedImage
 * @param y координата верхнего угла квадратной области BufferedImage
 * @param squareSize сторона квадратной области для анализа
 * @return массив из двух точек или пустой массив, если линия не найдена
 */
fun findVerticalLine(image: BufferedImage, x: Int, y: Int, squareSize: Int): List<Point> {
    var lineStart: Point? = null
    var lineEnd: Point? = null
    val raster = image.raster
    var pixel = IntArray(4)
    for(j in 0..<squareSize){
        raster.getPixel(x+j,y,pixel)
        if(pixel[0] == 0){
            lineStart = Point(x+j,y)
            for(i in 0..<squareSize){
                raster.getPixel(x+j,y+i,pixel)
                if(pixel[0] != 0){
                    lineEnd = Point(x+j,y+i-1)
                    break
                }
            }
            if(lineEnd == null){
                lineEnd = Point(x+j,y+squareSize-1)
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

