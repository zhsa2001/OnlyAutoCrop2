import javax.swing.JFileChooser

class TifFileChooser: JFileChooser() {
    init {
        this.setAcceptAllFileFilterUsed(false)
        this.isMultiSelectionEnabled = true
        this.addChoosableFileFilter(TiffFilter())
    }
}