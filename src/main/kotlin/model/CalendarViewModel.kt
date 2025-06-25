package model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Наследник ViewModel для обновления состояния экземпляра класса Calendar
 */
class CalendarViewModel : ViewModel() {
    private var _time = MutableStateFlow(Calendar.Builder().setTimeOfDay(5, 30, 0).build())
    val time = _time.asStateFlow()

    /**
     * Обновление значения _time
     * @param Calendar новое значение
     */
    fun setTime(newTime: Calendar) {
        _time.value = newTime
    }
}