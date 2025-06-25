package ui.fileChoose

import java.io.File
import javax.swing.filechooser.FileFilter

/**
 * Наследник FileFilter, применяемый для фильтраци показываемых в менеджере файлов директорий
 * и файлов с определенным форматом
 */
class TiffFilter: FileFilter() {
    override fun accept(pathname: File?): Boolean {
        return pathname!!.extension == "tiff" ||
                pathname.extension == "tif" ||
                pathname.isDirectory
    }

    override fun getDescription(): String {
        return "Tiff only"
    }
}