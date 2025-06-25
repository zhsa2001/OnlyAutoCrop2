package ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import imageProcessing.CropRegion
import imageProcessing.NewCropRegion
import imageProcessing.OldCropRegion
import logic.FileAndDiapasons
import logic.checkIsNum
import model.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.imageio.ImageIO
import kotlin.concurrent.thread

/**
 * Экран со всеми настройками, касающимися обрезки-соединения
 * @param getSetCropRegion для получения(при передаче null)-установки области обрезки CropRegion
 * @param onEraseHourSet функция, вызываемая при установке опции "закрашивания" часа в правом углу каждого листа,
 * true - при включении опции
 * @param onDesiredHeightSet функция, вызываемая при установке опции выбора высоты итогового изображения,
 * принимает в качестве параметра высоту результирующего изображения - если 0, то высота изображения не изменяется
 * @param onSeparateListSet функция, вызываемая при установке опции раздельного сохранения каждого листа,
 * true - при включении опции
 * @param onTimeOnFirstListSet функция для установки времени на первом листе при включенной опции отдельного сохранения листов
 * @param goNext переход к следующему этапу работы
 * @param returnToStart возвращение к началу работы
 */
@Composable
fun CropSetupScreen(
    getSetCropRegion: (CropRegion?) -> CropRegion,
    onEraseHourSet: (Boolean) -> Unit,
    onDesiredHeightSet: (Int) -> Unit,
    onSeparateListSet: (Boolean) -> Unit,
    onTimeOnFirstListSet: (Calendar) -> Unit,
    goNext: () -> Unit,
    returnToStart: () -> Unit
) {
    var cropMode by remember { mutableStateOf(0) }
    val cropModeText: (Int) -> String = { i ->
        when (i) {
            0 -> "Файл старой программы"
            1 -> "Файл новой программы"
            else -> "Ручная установка параметров"
        }
    }
    var x by remember { mutableStateOf(getSetCropRegion(null).x) }
    var y by remember { mutableStateOf(getSetCropRegion(null).y) }
    var w by remember { mutableStateOf(getSetCropRegion(null).w) }
    var h by remember { mutableStateOf(getSetCropRegion(null).h) }
    var imagePreview by remember { mutableStateOf(ImageBitmap(0, 0)) }

    var eraseHourOnEachList by remember { mutableStateOf(true) }
    var heightSet by remember { mutableStateOf(false) }
    var desiredHeight by remember { mutableStateOf(0) }
    var separateLists by remember { mutableStateOf(false) }

    val timeOnFirstPageViewModel by remember { mutableStateOf(CalendarViewModel()) }

    // при каждой перерисовке окна обновляются параметры
    onSeparateListSet(separateLists)
    onDesiredHeightSet(desiredHeight)
    onTimeOnFirstListSet(timeOnFirstPageViewModel.time.collectAsState().value)

    val windowScrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        // отдельный поток для чтения изображения
        thread {
            if (FileAndDiapasons.files.size > 0
                && FileAndDiapasons.files[0].file != null
                && FileAndDiapasons.files[0].file!!.exists()
            ) {
                try {
                    imagePreview = ImageIO.read(FileAndDiapasons.files[0].file!!).toComposeImageBitmap()
                } catch (_: Exception) {

                }
            }
        }
    }

    val updateTimeModel: (Int) -> Unit = {minutes ->
        val newTime = timeOnFirstPageViewModel.time.value.clone() as Calendar
        newTime.add(Calendar.MINUTE, minutes)
        timeOnFirstPageViewModel.setTime(newTime)
    }
    val timeOnFirstPageMinus90: ()  -> Unit = {
        updateTimeModel(-90)
    }

    val timeOnFirstPagePlus90: ()  -> Unit ={
        updateTimeModel(90)
    }

    val setX: (String) -> Unit = { if (checkIsNum(it)) x = if (it == "") 0 else it.toInt() }
    val setY: (String) -> Unit = { if (checkIsNum(it)) y = if (it == "") 0 else it.toInt() }
    val setW: (String) -> Unit = { if (checkIsNum(it)) w = if (it == "") 0 else it.toInt() }
    val setH: (String) -> Unit = { if (checkIsNum(it)) h = if (it == "") 0 else it.toInt() }
    val xText = "x-координата левого верхнего угла "
    val yText = "y-координата левого верхнего угла "
    val wText = "ширина области "
    val hText = "высота области"

    val saveListsSeparatelyText = "Сохранить листы отдельно"
    val eraseCheckBoxText = "Стереть номер часа в правом верхнем углу на каждом листе"
    val returnText = "Вернуться в начало"
    val processingText = "Обработать"


    val imageCropModifier = Modifier.drawWithContent {
        drawContent()
        val size = this.size
        val scale = size.width / imagePreview.width
        drawRect(
            color = Color.hsl(36f, 0.7f, 0.5f, 0.5f),
            topLeft = Offset(scale * x, scale * y),
            size = Size(scale * w, scale * h)
        )
    }

    val processImage = {
        when (cropMode) {
            0 -> getSetCropRegion(OldCropRegion())
            1 -> getSetCropRegion(NewCropRegion());
            2 -> getSetCropRegion(CropRegion(x, y, w, h));
        }
        onEraseHourSet(eraseHourOnEachList)
        goNext()
    }

    val changeEraseOnEachList: (Boolean) -> Unit = { eraseHourOnEachList = it}
    val setCropMode: (Int) -> Unit = { cropMode = it }



    Column(Modifier.verticalScroll(windowScrollState).fillMaxWidth()) {
        Row {
            Checkbox(checked = heightSet, onCheckedChange = { heightSet = it; desiredHeight = 0 })
            Text(
                "Задать высоту итогового изображения",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            if (heightSet){
                TextField(value = desiredHeight.toString(),
                    onValueChange = { if (checkIsNum(it)) desiredHeight = if (it == "") 0 else it.toInt(); })
            }
        }
        val setSeparatelySaving: (Boolean) -> Unit = {
            separateLists = it; timeOnFirstPageViewModel.setTime(
            Calendar.Builder().setTimeOfDay(5,30,0).build())
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = separateLists, onCheckedChange = setSeparatelySaving)
            Text(saveListsSeparatelyText, modifier = Modifier.align(Alignment.CenterVertically))
            if (separateLists){
                Button(onClick = timeOnFirstPageMinus90){
                    Text("<<")
                }
                Text(SimpleDateFormat("HH:mm").format(timeOnFirstPageViewModel.time.collectAsState().value.time))
                Button(onClick = timeOnFirstPagePlus90){
                    Text(">>")
                }
            }
        }
        Column(modifier = Modifier.selectableGroup()) {
            for (i in 0..2) {
                Row {
                    RadioButton(selected = cropMode == i, onClick = { setCropMode(i) })
                    Text(cropModeText(i), modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            if (cropMode == 2) {
                TextField(x.toString(), onValueChange = { setX(it) },
                    label = { Text(xText) })
                TextField(y.toString(), onValueChange = { setY(it) },
                    label = { Text(yText) })
                TextField(w.toString(), onValueChange = { setW(it) },
                    label = { Text(wText) })
                TextField(h.toString(), onValueChange = { setH(it) },
                    label = { Text(hText) })

                Column(modifier = imageCropModifier) {
                    Image(imagePreview, "Первое изображение из первого файла")
                }
            }
        }
        Row {
            Checkbox(checked = eraseHourOnEachList, onCheckedChange = changeEraseOnEachList)
            Text(eraseCheckBoxText, modifier = Modifier.align(Alignment.CenterVertically))
        }
        Button(onClick = processImage) {
            Text(processingText)
        }
        Button(onClick = returnToStart) {
            Text(returnText)
        }
    }
}
