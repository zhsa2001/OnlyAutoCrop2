package ui

import ui.fileChoose.TifFileChooser
import java.io.File
import javax.swing.JFileChooser

fun getFileFromChooseDialog(path: File?): File?{
    val fc = TifFileChooser(path)
    return if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        fc.selectedFile else null
}