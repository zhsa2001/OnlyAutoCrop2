import javax.swing.JFileChooser

class TifFileChooser: JFileChooser() {
    init {
        this.setAcceptAllFileFilterUsed(false)
        this.isMultiSelectionEnabled = false
        this.addChoosableFileFilter(TiffFilter())
    }
}