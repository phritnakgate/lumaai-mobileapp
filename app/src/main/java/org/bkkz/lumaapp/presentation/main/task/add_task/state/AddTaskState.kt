package org.bkkz.lumaapp.presentation.main.task.add_task.state

import org.bkkz.lumaapp.util.enums.ServiceState

data class AddTaskState(
    val name: String? = null,
    val description: String? = null,
    val isTimeSpecify: Boolean = false,
    val taskDate: String? = null,
    val taskTime: String? = null,
    val errorField : Map<RequiredFormField, Boolean?> = emptyMap(),
    val serviceState: ServiceState = ServiceState.IDLE
) {
    enum class RequiredFormField{
        TASK_NAME,
        TASK_DATE,
        TASK_TIME
    }
}
