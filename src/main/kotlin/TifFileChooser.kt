import java.io.File
import javax.swing.JFileChooser

class TifFileChooser(path: File?): JFileChooser(path) {
    init {
        this.setAcceptAllFileFilterUsed(false)
        this.isMultiSelectionEnabled = false
        this.addChoosableFileFilter(TiffFilter())
    }
}