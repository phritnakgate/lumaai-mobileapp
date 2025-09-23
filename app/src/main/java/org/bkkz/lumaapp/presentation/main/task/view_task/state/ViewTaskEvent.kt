package org.bkkz.lumaapp.presentation.main.task.view_task.state

sealed class ViewTaskEvent {
    data class OnUserSelectedMonth(val position : Int) : ViewTaskEvent()
    data class OnUserSelectedDate(val date : String) : ViewTaskEvent()
}