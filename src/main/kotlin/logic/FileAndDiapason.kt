package logic

import java.io.File

/**
 * Представляет файл и диапазон листов из него
 * @param file файл
 * @param start начало диапазона, начиная с 1
 * @param end конец диапазона, начиная с 1
 * */
data class FileAndDiapason(var file: File? = null, var start: Int? = 1, var end: Int? = 1)

/**
 * Объект для управления всеми файлами и их диапазонами
 */
object FileAndDiapasons{
    /**
     * Список всех выбранных файло-диапазонов
     */
    val files = mutableListOf<FileAndDiapason>()

    /**
     * Заменяет файло-диапазон в списке на новый, с новым файлом
     * @param index индекс заменяемого элемента
     * @param file новый файл
     */
    fun update(index: Int, file: File?){
        if (index in files.indices){
            files[index] = FileAndDiapason(file)
        }
    }

    /**
     * удаляет все из files
     */
    fun clear(){
        files.clear()
    }
}