package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import imageProcessing.cropAndConcatManyImages
import imageProcessing.CropRegion
import java.util.*

/**
 * Экран, на ктором отображается текущий процесс по задаче
 * @param cropRegion область обрезки
 * @param eraseHourOnEachList опция "закраски" номера часа в правом углу каждого листа
 * @param desiredHeight высота итогового изображения. Если 0, то высота не меняется
 * @param separateLists сохранять отдельными листами - true, или единым изображением - false
 * @param time время на первом листе, играет роль в случае раздельного сохранния листов
 * @param returnMessage для передачи сообщения с результатом выполнения задачи дальше
 * @param goNext переход к следующему этапу
 * @param returnToStart переход к началу
 */
@Composable
fun ProgressScreen(cropRegion: CropRegion, eraseHourOnEachList: Boolean,
                   desiredHeight: Int, separateLists: Boolean,
                   time: Calendar, returnMessage: (String) -> Unit, goNext:()-> Unit, returnToStart:()->Unit) {
    var progress by remember { mutableStateOf(0) }
    var closeTask by remember { mutableStateOf(false) }
    var curTask by remember { mutableStateOf("") }
    val cancelText = "Отменить"

    val cancel = {
        closeTask = true
        returnToStart()
    }

    LaunchedEffect(null){
        // основная задача выполняется в отдельном потоке
        Thread(){
            var resMessage = cropAndConcatManyImages(
                cropRegion,
                eraseHourOnEachList,
                { progress = it },
                { closeTask },
                { curTask = it},
                desiredHeight,
                separateLists,
                time
            )
            if (!closeTask) {
                returnMessage(resMessage)
                goNext()
            }
        }.start()
    }

    Column{
        Text("${curTask}: ${progress}% выполнено")
        Button(onClick = cancel) {
            Text(cancelText)
        }
    }
}
