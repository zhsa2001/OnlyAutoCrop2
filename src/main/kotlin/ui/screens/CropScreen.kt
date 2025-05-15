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
import imageProcessing.NewCropRegion_
import imageProcessing.OldCropRegion
import logic.FileAndDiapasons
import logic.checkIsNum
import javax.imageio.ImageIO
import kotlin.concurrent.thread

@Composable
fun CropScreen(
    getSetCropRegion: (CropRegion?) -> CropRegion,
    onEraseHourSet: (Boolean) -> Unit,
    onDesiredHeightSet: (Int) -> Unit,
    goNext: () -> Unit,
    returnToStart: () -> Unit
) {
    var cropMode by remember { mutableStateOf(0) }
    var x by remember { mutableStateOf(getSetCropRegion(null).x) }
    var y by remember { mutableStateOf(getSetCropRegion(null).y) }
    var w by remember { mutableStateOf(getSetCropRegion(null).w) }
    var h by remember { mutableStateOf(getSetCropRegion(null).h) }
    var imagePreview by remember { mutableStateOf(ImageBitmap(0, 0)) }
    //
    var eraseHourOnEachList by remember { mutableStateOf(true) }
    var heightSet by remember { mutableStateOf(false) }
    var desiredHeight by remember { mutableStateOf(0) }
    onDesiredHeightSet(desiredHeight)
    val state = rememberScrollState()
    LaunchedEffect(Unit) {
        state.animateScrollTo(100)
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
    Column(Modifier.verticalScroll(state).fillMaxWidth()) {
        Row {
            Checkbox(checked = heightSet, onCheckedChange = { heightSet = it; desiredHeight = 0 })
            Text(
                "Задать высоту итогового изображения",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            if (heightSet){
                TextField(value = desiredHeight.toString(),
                    onValueChange = { if (checkIsNum(it)) desiredHeight = if (it == "") 0 else it.toInt();

                    })
            }
        }
        Column(modifier = Modifier.selectableGroup()) {
            for (i in 0..2) {
                Row {
                    RadioButton(selected = cropMode == i, onClick = { cropMode = i })
                    Text(
                        when (i) {
                            0 -> "Файл старой программы"
                            1 -> "Файл новой программы"
                            else -> "Ручная установка параметров"
                        }, modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            if (cropMode == 2) {
                TextField(x.toString(), onValueChange = {
                    val xPrev = x; if (checkIsNum(it)) x = if (it == "") 0 else it.toInt();
//                    w -= (x - xPrev)
                },
                    label = { Text("x-координата левого верхнего угла ") })
                TextField(y.toString(), onValueChange = {
                    val yPrev = y; if (checkIsNum(it)) y = if (it == "") 0 else it.toInt();
//                    h -= (y - yPrev)
                },
                    label = { Text("y-координата левого верхнего угла ") })

                TextField(w.toString(), onValueChange = { if (checkIsNum(it)) w = if (it == "") 0 else it.toInt() },
                    label = { Text("ширина области ") })

                TextField(h.toString(), onValueChange = { if (checkIsNum(it)) h = if (it == "") 0 else it.toInt() },
                    label = { Text("высота области") })

                Column(
                    modifier = Modifier.drawWithContent {
                        drawContent()
                        val size = this.size
                        val scale = size.width / imagePreview.width
                        drawRect(
                            color = Color.hsl(36f, 0.7f, 0.5f, 0.5f),
                            topLeft = Offset(scale * x, scale * y),
                            size = Size(scale * w, scale * h)
                        )
                    }

                ) {
                    Image(
                        imagePreview, "Первое изображение из первого файла",
                    )
                }
            }

        }
        Row {
            Checkbox(checked = eraseHourOnEachList, onCheckedChange = { eraseHourOnEachList = !eraseHourOnEachList })
            Text(
                "Стереть номер часа в правом верхнем углу на каждом листе",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Button(onClick = {
            when (cropMode) {
                0 -> getSetCropRegion(OldCropRegion())
                1 -> getSetCropRegion(NewCropRegion_());
                2 -> getSetCropRegion(CropRegion(x, y, w, h));
            }
            onEraseHourSet(eraseHourOnEachList)
            goNext()
        }) {
            Text("Обработать")
        }


        Button(onClick = {
            returnToStart()
        }) {
            Text("Вернуться в начало")
        }
    }
}
