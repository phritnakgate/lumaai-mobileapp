package org.bkkz.lumaapp.presentation.main.task.view_task.state

import java.time.YearMonth

sealed class ViewTaskEvent {
    data object LoadFirstTimeTasks : ViewTaskEvent()
    data class OnUserSelectedMonth(val position : Int, val selectedMonth : YearMonth) : ViewTaskEvent()
    data class OnUserSelectedDate(val date : String) : ViewTaskEvent()
}