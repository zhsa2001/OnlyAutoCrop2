import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import imageProcessing.CropRegion
import logic.FileAndDiapasons
import ui.UIStage
import ui.screens.CropSetupScreen
import ui.screens.OriginalFilesScreen
import ui.screens.ProgressScreen
import ui.screens.ResultOfCropping
import java.io.File
import java.util.Calendar

fun main() = application {
    val width = 500.dp
    val height = 600.dp
    val AppIcon = painterResource("icon.png")
    Window(
        onCloseRequest = ::exitApplication,
        title = "Соединение tif в png",
        icon = AppIcon,
        state = WindowState(width = width, height = height)
    ) {
        App()
    }
}


/**
 * Приложение
 */
@Composable
fun App() {
    var stage by remember { mutableStateOf(UIStage.START) }
    var path by remember { mutableStateOf<File?>(null) }
    var message by remember { mutableStateOf("") }
    var cropRegion: CropRegion by remember { mutableStateOf(CropRegion()) }
    val returnToStart: () -> Unit = { stage = UIStage.START }
    var eraseHourOnEachList by remember { mutableStateOf(true) }
    var desiredHeight by remember { mutableStateOf(0) }
    var timeOnFirstList by remember { mutableStateOf(Calendar.getInstance()) }
    var separateLists by remember { mutableStateOf(false) }

    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)) {
            when (stage) {
                UIStage.START -> OriginalFilesScreen(
                    { stage = UIStage.FILE_CHOOSEN },
                    {
                        it?.let {
                            path = it
                        }
                        path
                    })

                UIStage.FILE_CHOOSEN -> CropSetupScreen(
                    {
                        it?.let { cropRegion = it }
                        cropRegion
                    },
                    { eraseHourOnEachList = it },
                    { desiredHeight = it },
                    { separateLists = it },
                    { timeOnFirstList = it },
                    { stage = UIStage.CROPPING },
                    returnToStart
                )

                UIStage.CROPPING -> ProgressScreen(
                    cropRegion,
                    eraseHourOnEachList,
                    desiredHeight,
                    separateLists,
                    timeOnFirstList,
                    { message = it },
                    { stage = UIStage.CROPPING_IS_DONE },
                    returnToStart
                )

                UIStage.CROPPING_IS_DONE -> {
                    ResultOfCropping(message, returnToStart)
                    FileAndDiapasons.clear()
                }
            }
        }
    }
}