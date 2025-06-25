package ui

import ui.fileChoose.TifFileChooser
import java.io.File
import javax.swing.JFileChooser

/**
 * Получает с использованием файлового мененджера tiff-файл от пользователя
 * @param path директория для открытия файлового менеджера
 * @return выбранный файл или null, если пользователь не выбрал ничего
 */
fun getFileFromChooseDialog(path: File?): File?{
    val fc = TifFileChooser(path)
    return if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        fc.selectedFile else null
}