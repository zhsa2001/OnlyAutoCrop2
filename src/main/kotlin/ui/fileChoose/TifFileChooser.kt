package ui.fileChoose

import java.io.File
import javax.swing.JFileChooser

/**
 * Наследник JFileChooser для выбора единственного tiff-файла из менеджера файлов
 * @param path директория, которая открывается при запуске менеджера файлов.
 * Если передается null, то открывается директория по умолчанию
 */
class TifFileChooser(path: File?): JFileChooser(path) {
    init {
        this.setAcceptAllFileFilterUsed(false)
        this.isMultiSelectionEnabled = false
        this.addChoosableFileFilter(TiffFilter())
    }
}