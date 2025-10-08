package org.bkkz.lumaapp.presentation.main.task.edit_task.state

import org.bkkz.lumaapp.util.enums.ServiceState

data class EditTaskState(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val isTimeSpecify: Boolean = false,
    val taskDate: String? = null,
    val taskTime: String? = null,
    val priority: Int? = null,
    val category: Int? = null,
    val errorField : Map<RequiredFormField, Boolean?> = emptyMap(),
    val serviceState: ServiceState = ServiceState.IDLE
) {
    enum class RequiredFormField{
        TASK_NAME,
        TASK_DATE,
        TASK_TIME
    }
}
