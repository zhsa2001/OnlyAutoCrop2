package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import imageProcessing.cropAndConcatManyImages
import imageProcessing.CropRegion
import java.util.*

@Composable
fun ProgressScreen(cropRegion: CropRegion, eraseHourOnEachList: Boolean,
                   desiredHeight: Int, separateLists: Boolean,
                   time: Calendar, returnMessage: (String) -> Unit, changeState:()-> Unit, returnToStart:()->Unit) {
    var progress by remember { mutableStateOf(0) }
    var closeTask by remember { mutableStateOf(false) }
    var curTask by remember { mutableStateOf("") }

    LaunchedEffect(null){
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
                changeState()
            }


        }.start()
    }

    Column{
        Text("${curTask}: ${progress}% выполнено")
        Button(onClick = {
            closeTask = true
            returnToStart()
        }) {
            Text("Отменить")
        }
    }
}
