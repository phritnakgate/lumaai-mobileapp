package org.bkkz.lumaapp.presentation.main.task.edit_task.state

import org.bkkz.lumaapp.data.entity.task.Task


sealed class EditTaskEvent {
    data class InitData(val task : Task) : EditTaskEvent()
    data class OnChangeName(val name : String) : EditTaskEvent()
    data class OnChangeDescription(val desc : String) : EditTaskEvent()
    data class OnCheckTimeSpecified(val chk : Boolean) : EditTaskEvent()
    data class OnSelectedDate(val date : String) : EditTaskEvent()
    data class OnSelectedTime(val time : String) : EditTaskEvent()
    data object OnEditTask : EditTaskEvent()
    data object OnDeleteTask : EditTaskEvent()
}