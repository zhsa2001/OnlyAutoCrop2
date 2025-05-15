package models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

class CalendarViewModel : ViewModel() {
    private var _time = MutableStateFlow(Calendar.Builder().setTimeOfDay(5, 30, 0).build())
    val time = _time.asStateFlow()

    fun setTime(newTime: Calendar) {
        _time.value = newTime


    }
}