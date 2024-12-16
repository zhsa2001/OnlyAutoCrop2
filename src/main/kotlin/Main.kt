import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import javax.imageio.ImageIO
import kotlin.concurrent.thread

fun main() = application {
    val width = 500.dp
    val height = 600.dp
    val AppIcon = painterResource("icon.png")
    Window(onCloseRequest = ::exitApplication,title = "Соединение tif в png", icon = AppIcon, state = WindowState(width = width, height = height)) {
        App()
    }
}

data class FileAndDiapason(var file: File? = null, var start: Int? = 1, var end: Int? = 1)

object FileAndDiapasons{
    val files = mutableListOf<FileAndDiapason>()
    fun add(){

    }
    fun update(index: Int, file: File?){
        if (index in files.indices){
            files[index] = FileAndDiapason(file)
        }
    }

    fun clear(){
        files.clear()
    }
}

@Composable
fun App() {
    var stage by remember { mutableStateOf(UIStage.START) }
//    var files by remember { mutableStateOf(FileAndDiapasons) }
    var path by remember { mutableStateOf<File?>(null) }
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
                UIStage.START -> ManyFilesScreen({ stage = UIStage.FILE_CHOOSEN },
                    {
                        it?.let{
                            path = it
                        }
                        return@ManyFilesScreen path
                    })
                    /* MainScreen(
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

                     */
                UIStage.FILE_CHOOSEN -> CropScreen(
                    {
                        it?.let { cropRegion = it }
                        return@CropScreen cropRegion
                    },
                    { eraseHourOnEachList = it },
                    { stage = UIStage.CROPPING },
                    { returnToStart() }
                )
                UIStage.CROPPING -> ProgressScreen3(cropRegion!!,

                    /*ProgressScreen2(file1!!,file2!!,
                    list1,list2,part2,
                    cropRegion!!,

                     */
                    eraseHourOnEachList,
                    { message = it},
                    { stage = UIStage.CROPPING_IS_DONE },
                    { returnToStart() }
                )
                UIStage.CROPPING_IS_DONE -> {
                    ResultOfCropping(message,
                        { returnToStart() }
                    )
                    FileAndDiapasons.clear()
                }
            }
        }
    }
}

@Composable
fun oddEvenScreen(){

}

@Composable
fun ManyFilesScreen(goNext: () -> Unit, path: (File?) -> File?){
    var files by remember { mutableStateOf(FileAndDiapasons) }
    var filesListSize by remember { mutableStateOf(files.files.size)}
    Column {

        Row {
            Button(onClick = {
                files.files.add(FileAndDiapason())
                filesListSize++
            },
                modifier = Modifier.padding(0.dp,0.dp,8.dp,0.dp)
            ){
                Text("Добавить файл и диапазон")
            }
            Button(onClick = {
//                files.files.add(FileAndDiapason())
//                filesListSize++
                goNext()
            }){
                Text("Далее")
            }
        }


        for (i in 0..<filesListSize){
            Row (modifier = Modifier.padding(8.dp)) {
                Button(onClick = {
                    val file = getFileFromChooseDialog(path(null))

                    file?. let {
                        files.update(i,file);
                        path(File(file.parent))
                    }
                    filesListSize = -1
                    filesListSize = files.files.size},
                    modifier = Modifier.align(Alignment.CenterVertically)){
                    Text("Выбрать файл")
                }
                Button(onClick = {
                    files.files.removeAt(i)
                    filesListSize--
                },
                    modifier = Modifier.align(Alignment.CenterVertically)
                    ){
                    Text("–")
                }
                files.files[i].file?.let {
                    Text(files.files[i].file!!.name, modifier = Modifier.width(300.dp).padding(10.dp,0.dp).align(Alignment.CenterVertically))
//                    OutlinedTextField(if (files.files[i].start != null) files.files[i].start.toString() else "",
//                        onValueChange = {
//                            println(it)
//                            if (it == ""){
//                                files.files[i].start = null
//                            } else if (checkIsNum(it)){
//                                filesListSize = -1
//                                files.files[i].start = it.toInt()
//                                filesListSize = files.files.size
//                            }
//                        },
//                        modifier = Modifier.width(80.dp)
//                    )
//                    Text(" - ")
////                    OutlinedTextField("111\n11", onValueChange = {})
//                    OutlinedTextField(if (files.files[i].end != null) files.files[i].end.toString() else "",
//                        onValueChange = {
//                            println(it)
//                            if (it == ""){
//                                files.files[i].end = null
//                            } else if (checkIsNum(it)){
//                                filesListSize = -1
//                                files.files[i].end = it.toInt()
//                                filesListSize = files.files.size
//                            }
//                        },
//                        modifier = Modifier.width(80.dp)
//                    )
                    OutlinedTextField(
                                "${if (files.files[i].start == null) "" else files.files[i].start} -" +
                                        " ${if (files.files[i].end == null) "" else files.files[i].end}",
                        onValueChange = {
                            val nums = it.replace(" ","").split("-")
                            for(j in 0..1) {
                                if (nums.size > j) {
                                    if (nums[j] == "") {
                                        if (j == 0)
                                            files.files[i].start = null
                                        else
                                            files.files[i].end = null
                                    } else if (checkIsNum(nums[j])) {
                                        if (j == 0)
                                            files.files[i].start = nums[j].toInt()
                                        else
                                            files.files[i].end = nums[j].toInt()
                                    }
                                }
                            }
//
//                            if (it == ""){
//                                files.files[i].end = null
//                            } else if (checkIsNum(it)){
//
//                                files.files[i].end = it.toInt()
//
//                            }
                            filesListSize = -1
                            filesListSize = files.files.size
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }

            }
        }

    }
}


@Composable
fun ProgressScreen3(cropRegion: CropRegion, eraseHourOnEachList: Boolean, returnMessage: (String) -> Unit, changeState:()-> Unit, returnToStart:()->Unit) {
    var progress by remember { mutableStateOf(0) }
    var closeTask by remember { mutableStateOf(false) }
    var curTask by remember { mutableStateOf("") }

    LaunchedEffect(null){
        Thread(){
            var resMessage = cropAndConcatManyImages(
                    FileAndDiapasons,
                    cropRegion,
                    eraseHourOnEachList,
                    { progress = it },
                    { return@cropAndConcatManyImages closeTask },
                    { curTask = it}
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
                val file1 = getImageSource(null)
                file1GetSet(file1)

            }) {
                Text("Выбрать первый файл")
            }
            file1GetSet(null)?.name?.let { Text(it,modifier = Modifier.align(Alignment.CenterVertically),fontStyle = FontStyle.Italic) }
        }
        Row {
            Button(onClick = {
                val file2 = getImageSource(null)
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
    var imagePreview by remember { mutableStateOf(ImageBitmap(0,0)) }
    //
    var eraseHourOnEachList by remember { mutableStateOf(true) }
    val state = rememberScrollState()
    LaunchedEffect(Unit) {
        state.animateScrollTo(100)
//        coroutineScope {
            thread {
                if (FileAndDiapasons.files.size > 0
                    && FileAndDiapasons.files[0].file != null
                    && FileAndDiapasons.files[0].file!!.exists()){
                    try {
                        imagePreview = ImageIO.read(FileAndDiapasons.files[0].file!!).toComposeImageBitmap()
                    } catch (e: Exception){

                    }
                }
//            }


        }
    }
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
                TextField(x.toString(),onValueChange = { val xPrev = x; if (checkIsNum(it)) x = if(it == "") 0 else it.toInt();
//                    w -= (x - xPrev)
                                                       },
                    label = { Text("x-координата левого верхнего угла ") })
                TextField(y.toString(),onValueChange = { val yPrev = y; if (checkIsNum(it)) y = if(it == "") 0 else it.toInt();
//                    h -= (y - yPrev)
                                                       },
                    label = { Text("y-координата левого верхнего угла ") })

                TextField(w.toString(),onValueChange = { if (checkIsNum(it)) w = if(it == "") 0 else it.toInt() },
                    label = { Text("ширина области ") })

                TextField(h.toString(),onValueChange = { if (checkIsNum(it)) h = if(it == "") 0 else it.toInt() },
                    label = { Text("высота области") })

                Column (
                    modifier = Modifier.drawWithContent {
////                        drawRect(
////
////                                Color.Transparent
////
////                        )
                        drawContent()
                        val size = this.size
                        val scale = size.width / imagePreview.width
//                        drawImage(imagePreview., topLeft = Offset(x.toFloat(),y.toFloat()))
                        drawRect(color = Color.hsl(36f,0.7f,0.5f,0.5f), topLeft = Offset(scale*x,scale*y), size = Size(scale*w,scale*h))
                    }

                ){
                    Image(imagePreview,"Первое изображение из первого файла",
//                        Modifier.graphicsLayer {
//                            this.
//                        }
                    )
                }
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

fun getImageSource(path: File?): File? = getFileFromChooseDialog(path)
