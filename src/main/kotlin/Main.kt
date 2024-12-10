import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ui.filechooser.UIStage
import java.io.File

fun main() = application {
    val width = 500.dp
    val height = 600.dp
    val AppIcon = painterResource("icon.png")
    Window(onCloseRequest = ::exitApplication,title = "Соединение tif в png", icon = AppIcon, state = WindowState(width = width, height = height)) {
        App()
    }
}

@Composable
fun App() {
    var stage by remember { mutableStateOf(UIStage.START) }
    var sourceFile by remember { mutableStateOf<File?>(null) }
//    var files by remember { mutableStateOf<List<File>?>(null) }
    var message by remember { mutableStateOf("") }
    var cropRegion: CropRegion by remember { mutableStateOf(CropRegion())}
    var file1 by remember { mutableStateOf<File?>(null) }
    var file2 by remember { mutableStateOf<File?>(null) }
    val returnToStart: () -> Unit = { stage = UIStage.START }
    var eraseHourOnEachList by remember { mutableStateOf(true) }

    var list1 by remember { mutableStateOf(0) }
    var list2 by remember { mutableStateOf(0) }
    var part2 by remember { mutableStateOf(true) }

    MaterialTheme {
        Box(modifier = Modifier.padding(12.dp)){
            when(stage){
                UIStage.START -> MainScreen(
                    {
                        it?.let { file1 = it }
                        return@MainScreen file1
                    },
                    {
                        it?.let { file2 = it }
                        return@MainScreen file2
                    },
                    {
                        part2 = it
                    },
                    {
                        list1 = it
                    },
                    {
                        list2 = it
                    },
                    { stage = UIStage.FILE_CHOOSEN }
                    )
                UIStage.FILE_CHOOSEN -> CropScreen(
                    {
                        it?.let { cropRegion = it }
                        return@CropScreen cropRegion
                    },
                    { eraseHourOnEachList = it },
                    { stage = UIStage.CROPPING },
                    { returnToStart() }
                )
                UIStage.CROPPING -> ProgressScreen2(file1!!,file2!!,
                    list1,list2,part2,
                    cropRegion!!,
                    eraseHourOnEachList,
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
fun MainScreen(file1GetSet:(File?)->File?,
               file2GetSet:(File?)->File?,
               onPart2Selected:(Boolean)->Unit,
               onListStartSelected:(Int) -> Unit,
               onListStart2Selected:(Int) -> Unit,
               goNext:() -> Unit){

    var list1 by remember { mutableStateOf(0) }
    var list2 by remember { mutableStateOf(0) }
    var radio2 by remember { mutableStateOf(true) }
    val state = rememberScrollState()
    LaunchedEffect(Unit) { state.animateScrollTo(100) }
    Column(Modifier.verticalScroll(state).fillMaxWidth()){
        Row{
            Button(onClick = {
                val file1 = getImageSource()
                file1GetSet(file1)

            }) {
                Text("Выбрать первый файл")
            }
            file1GetSet(null)?.name?.let { Text(it,modifier = Modifier.align(Alignment.CenterVertically),fontStyle = FontStyle.Italic) }
        }
        Row {
            Button(onClick = {
                val file2 = getImageSource()
                file2GetSet(file2)
            }) {
                Text("Выбрать второй файл")
            }
            file2GetSet(null)?.name?.let { Text(it,modifier = Modifier.align(Alignment.CenterVertically),fontStyle = FontStyle.Italic) }
        }
        Text("Середина графика в")
        Row {
            RadioButton(selected = radio2, onClick = {radio2 = true;} )
            Text("Первом файле",
                modifier = Modifier.align(Alignment.CenterVertically))
            RadioButton(selected = !radio2, onClick = {radio2 = false;} )
            Text("Втором файле",
                modifier = Modifier.align(Alignment.CenterVertically))
        }


        TextField(list1.toString(),onValueChange = { if (checkIsNum(it)) list1 = if(it == "") 0 else it.toInt() },
            label = { Text("Номер листа начала основной части") })
        TextField(list2.toString(),onValueChange = { if (checkIsNum(it)) list2 = if(it == "") 0 else it.toInt() },
            label = { Text("Номер листа начала конца") })
        if (file1GetSet(null) != null && file2GetSet(null)  != null){
            Button(onClick =
            {
                onListStartSelected(list1)
                onListStart2Selected(list2)
                onPart2Selected(radio2)
                goNext() }){
                Text("Продолжить")
            }
        }
    }
}

@Composable
fun CropScreen(getSetCropRegion:(CropRegion?)->CropRegion, onEraseHourSet:(Boolean)->Unit, goNext:() -> Unit, returnToStart:()->Unit){
    var cropMode by remember { mutableStateOf(0)}
    var x by remember { mutableStateOf(getSetCropRegion(null).x) }
    var y by remember { mutableStateOf(getSetCropRegion(null).y) }
    var w by remember { mutableStateOf(getSetCropRegion(null).w) }
    var h by remember { mutableStateOf(getSetCropRegion(null).h) }
    var eraseHourOnEachList by remember { mutableStateOf(true) }
    val state = rememberScrollState()
    LaunchedEffect(Unit) { state.animateScrollTo(100) }
    Column(Modifier.verticalScroll(state).fillMaxWidth()) {
        Column ( modifier = Modifier.selectableGroup()){
            for(i in 0..2){
                Row {
                    RadioButton(selected = cropMode == i, onClick = {cropMode = i} )
                    Text(when(i){
                        0 -> "Файл старой программы"
                        1 -> "Файл новой программы"
                        else -> "Ручная установка параметров"
                    }, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
            if (cropMode == 2){
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
        Row {
            Checkbox(checked = eraseHourOnEachList, onCheckedChange = {eraseHourOnEachList = !eraseHourOnEachList})
            Text("Стереть номер часа в правом верхнем углу на каждом листе", modifier = Modifier.align(Alignment.CenterVertically))
        }
        Button(onClick = {
            when(cropMode){
                0 -> getSetCropRegion(OldCropRegion())
                1 -> getSetCropRegion(NewCropRegion_());
                2 -> getSetCropRegion(CropRegion(x,y,w,h));
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

@Composable
fun ProgressScreen2(file1: File, file2:File,list1: Int,list2: Int, part2: Boolean,
                    cropRegion: CropRegion, eraseHourOnEachList: Boolean, returnMessage: (String) -> Unit, changeState:()-> Unit, returnToStart:()->Unit){
    var progress by remember { mutableStateOf(0) }
    var closeTask by remember { mutableStateOf(false) }
    var curTask by remember { mutableStateOf("") }
    LaunchedEffect(null){
        Thread(){
            val parts = arrayOf(true,false)
            val v = arrayOf("v1","v2")
            var resMessage = ""
            for(i in parts.indices) {
                curTask = v[i]
                resMessage += cropAndConcateImagesStartEnd(
                    file1,
                    file2,
                    list1,
                    list2,
                    parts[i],
                    part2,
                    !parts[i],
                    v[i],
                    cropRegion,
                    eraseHourOnEachList,
                    { progress = it },
                    { return@cropAndConcateImagesStartEnd closeTask }
                )
            }
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

fun getImageSource(): File? = getFileFromChooseDialog()
