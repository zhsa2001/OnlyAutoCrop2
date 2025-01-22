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

@Composable
fun ManyFilesScreen(goNext: () -> Unit, path: (File?) -> File?){
    var files by remember { mutableStateOf(FileAndDiapasons) }
    var filesListSize by remember { mutableStateOf(files.files.size) }
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
                    Text(files.files[i].file!!.name, modifier = Modifier.width(300.dp).padding(10.dp,0.dp).align(
                        Alignment.CenterVertically))
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