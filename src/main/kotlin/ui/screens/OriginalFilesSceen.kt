package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import logic.FileAndDiapason
import logic.FileAndDiapasons
import logic.checkIsNum
import ui.getFileFromChooseDialog
import java.io.File

/**
 * Стартовый экран с выбором файлов и диапазонов листов в них
 * @param goNext переход к следующему этапу работы
 * @param path директория, которая открывается при запуске менеджера файлов.
 * Если передается null, то открывается директория по умолчанию
 */
@Composable
fun OriginalFilesScreen(goNext: () -> Unit, path: (File?) -> File?){
    val files by remember { mutableStateOf(FileAndDiapasons) }
    var filesListSize by remember { mutableStateOf(files.files.size) }

    val addRow: () -> Unit = {
        files.files.add(FileAndDiapason())
        filesListSize++
    }
    val setFile: (Int) -> Unit = { i ->
        val file = getFileFromChooseDialog(path(null))
        file?. let {
            files.update(i,file);
            path(File(file.parent))
        }
        filesListSize = -1
        filesListSize = files.files.size
    }
    val removeRow: (Int) -> Unit = {i ->
        files.files.removeAt(i)
        filesListSize--
    }
    val getDiapasonesString: (Int) -> String = { i ->
        "${if (files.files[i].start == null) "" else files.files[i].start} -" +
            " ${if (files.files[i].end == null) "" else files.files[i].end}"}
    val changeDiapason: (Int, String) -> Unit = { i, it ->
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
        filesListSize = -1
        filesListSize = files.files.size
    }

    val addRowText = "Добавить файл и диапазон"
    val nextText = "Далее"
    val setFileText = "Выбрать файл"
    val removeText = "–"

    val paddingModifier = Modifier.padding(8.dp)
    val fileNameModifier = Modifier.width(300.dp).padding(10.dp,0.dp)
    val diapasonTextFieldModifier = Modifier.width(80.dp)

    Column {
        Row {
            Button(onClick = addRow, modifier = paddingModifier){
                Text(addRowText)
            }
            Button(onClick = goNext, modifier = paddingModifier){
                Text(nextText)
            }
        }


        for (i in 0..<filesListSize){
            Row (modifier = paddingModifier) {
                Button(onClick = { setFile(i) },
                    modifier = Modifier.align(Alignment.CenterVertically)){
                    Text(setFileText)
                }
                Button(onClick = { removeRow(i) },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ){
                    Text(removeText)
                }

                files.files[i].file?.let {
                    Text(files.files[i].file!!.name, modifier = fileNameModifier.align(Alignment.CenterVertically))
                    OutlinedTextField(
                        getDiapasonesString(i),
                        onValueChange = { changeDiapason(i, it) },
                        modifier = diapasonTextFieldModifier
                    )
                }
            }
        }
    }
}