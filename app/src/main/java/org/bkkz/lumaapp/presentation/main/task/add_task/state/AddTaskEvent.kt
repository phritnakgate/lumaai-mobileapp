package org.bkkz.lumaapp.presentation.main.task.add_task.state

sealed class AddTaskEvent {
    data class OnChangeName(val name : String) : AddTaskEvent()
    data class OnChangeDescription(val desc : String) : AddTaskEvent()
    data class OnCheckTimeSpecified(val chk : Boolean) : AddTaskEvent()
    data class OnSelectedDate(val date : String) : AddTaskEvent()
    data class OnSelectedTime(val time : String) : AddTaskEvent()
    data object OnCreateTask : AddTaskEvent()
}