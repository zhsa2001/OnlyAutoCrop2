import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowState
import ui.filechooser.UIStage
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource

fun main() = application {
    val width = 500.dp
    val height = 300.dp
    val AppIcon = painterResource("icon.png")


    Window(onCloseRequest = ::exitApplication,title = "Соединение tif в png", icon = AppIcon, state = WindowState(width = width, height = height)) {
        App()
    }
}

//object AppIcon : Painter() {
//    override val intrinsicSize = Size(256f, 256f)
//
//    override fun DrawScope.onDraw() {
//        val im: BufferedImage = ImageIO.read(File("icon.png"))
//        val im2 = BufferedImage(size.width.toInt(),size.height.toInt(),BufferedImage.TYPE_INT_ARGB)
//        val draw = im2.createGraphics()
//        val koef = im2.width/im.width.toDouble()
//        draw.scale(koef,koef)
//        draw.drawImage(im,0,0,null)
//        drawImage(im2.toComposeImageBitmap())
//    }
//}

@Composable
fun App() {
    var stage by remember { mutableStateOf(UIStage.START) }
    var sourceFile by remember { mutableStateOf<File?>(null) }
    var files by remember { mutableStateOf<List<File>?>(null) }
    var message by remember { mutableStateOf("") }
    var cropRegion: CropRegion by remember { mutableStateOf(CropRegion())}

    val returnToStart:()->Unit = {stage = UIStage.START}

    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)){
            when(stage){
                UIStage.START -> MainScreen({
                    sourceFile = it
                    sourceFile?.let {
                        stage = UIStage.FILE_CHOOSEN
                    }
                },
                    {
                        files = it
                        files?.let {
                            stage = UIStage.FILE_CHOOSEN
                        }
                    })
                UIStage.FILE_CHOOSEN -> CropScreen(
                    { cropRegion = it },
                    { stage = UIStage.CROPPING },
                    { returnToStart() }
                )
                UIStage.CROPPING -> ProgressScreen(files!!,
                    cropRegion!!,
                    { message = it},
                    { stage = UIStage.CROPPING_IS_DONE },
                    { returnToStart() }
                )
                UIStage.CROPPING_IS_DONE -> ResultOfCropping(message,
                    { returnToStart() }
                )
            }
        }
    }
}


@Composable
fun MainScreen(onFileDialogFinished:(File?)->Unit, onFileDialogFinished2:(List<File>?)->Unit){
    Column {
        Button(onClick = {
            val file = getImageSource()
            onFileDialogFinished2(file)
        }) {
            Text("Выбрать изображение")
        }
    }
}

@Composable
fun CropScreen(setCropRegion:(CropRegion)->Unit,goNext:() -> Unit, returnToStart:()->Unit){
    var radioButtonNum by remember { mutableStateOf(0)}
    var x by remember { mutableStateOf(270) }
    var y by remember { mutableStateOf(70) }
    var w by remember { mutableStateOf(400) }
    var h by remember { mutableStateOf(400) }
    val state = rememberScrollState()
    LaunchedEffect(Unit) { state.animateScrollTo(100) }
    Column(Modifier.verticalScroll(state).fillMaxWidth()) {
        Column ( modifier = Modifier.padding(12.dp).selectableGroup()){
            for(i in 0..2){
                Row {
                    RadioButton(selected = radioButtonNum == i, onClick = {radioButtonNum = i} )
                    Text(when(i){
                        0 -> "Файл старой программы"
                        1 -> "Файл новой программы"
                        else -> "Ручная установка параметров"
                    }, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            if (radioButtonNum == 2){
                TextField(x.toString(),onValueChange = { if (checkIsNum(it)) x = if(it == "") 0 else it.toInt() },
                    label = { Text("x-координата левого верхнего угла ") })
                TextField(y.toString(),onValueChange = { if (checkIsNum(it)) y = if(it == "") 0 else it.toInt() },
                    label = { Text("y-координата левого верхнего угла ") })

                TextField(w.toString(),onValueChange = { if (checkIsNum(it)) w = if(it == "") 0 else it.toInt() },
                    label = { Text("ширина области ") })

                TextField(h.toString(),onValueChange = { if (checkIsNum(it)) h = if(it == "") 0 else it.toInt() },
                    label = { Text("высота области") })

            }
        }
        Button(onClick = {
            when(radioButtonNum){
                0 -> setCropRegion(OldCropRegion())
                1 -> setCropRegion(NewCropRegion_());
                2 -> setCropRegion(CropRegion(x,y,w,h));
            }
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

@Composable
fun ProgressScreen( files:  List<File>, cropRegion: CropRegion, returnMessage: (String) -> Unit, changeState:()-> Unit, returnToStart:()->Unit){
    var progress by remember { mutableStateOf(0) }
    var closeTask by remember { mutableStateOf(false) }
    var currentFile by remember { mutableStateOf("") }

    LaunchedEffect(null){
        Thread(){
            val resMessage = cropAndConcateImages(
                files,
                cropRegion,
                { progress = it },
                { return@cropAndConcateImages closeTask; },
                { currentFile = it}
            )
            if(!closeTask){
                returnMessage(resMessage)
                changeState()
            }
        }.start()
    }


    Column{
        Text("${currentFile}\n${progress}% выполнено")
        Button(onClick = {
            closeTask = true
            returnToStart()
        }) {
            Text("Отменить")
        }
    }
}

@Composable
fun ResultOfCropping(message: String, returnToStart:()->Unit){
    Column(

    ) {
        Column{
            Button(onClick = {
                returnToStart()
            }
            ) {
                Text("Вернуться в начало")
            }
        }
        Column{
            val state = rememberScrollState()
            LaunchedEffect(Unit) { state.animateScrollTo(100) }
            Text(message,Modifier.verticalScroll(state))
        }


    }
}

fun getImageSource(): List<File>? = getFileFromChooseDialog()?.toList()
