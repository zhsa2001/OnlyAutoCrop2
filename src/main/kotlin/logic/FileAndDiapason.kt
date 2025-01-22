package logic

import java.io.File

data class FileAndDiapason(var file: File? = null, var start: Int? = 1, var end: Int? = 1)


object FileAndDiapasons{
    val files = mutableListOf<FileAndDiapason>()

    fun update(index: Int, file: File?){
        if (index in files.indices){
            files[index] = FileAndDiapason(file)
        }
    }

    fun clear(){
        files.clear()
    }
}