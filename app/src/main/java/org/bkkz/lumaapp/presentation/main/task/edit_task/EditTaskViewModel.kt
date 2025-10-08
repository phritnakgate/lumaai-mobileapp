package org.bkkz.lumaapp.presentation.main.task.edit_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.entity.task.EditTaskRequest
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.task.edit_task.state.EditTaskEvent
import org.bkkz.lumaapp.presentation.main.task.edit_task.state.EditTaskState
import org.bkkz.lumaapp.util.enums.ServiceState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class EditTaskViewModel(private val repository: Repository) : ViewModel() {
    private val _state: MutableStateFlow<EditTaskState> = MutableStateFlow(EditTaskState())
    val state: StateFlow<EditTaskState> = _state.asStateFlow()

    fun onEvent(event: EditTaskEvent) {
        when (event) {
            is EditTaskEvent.InitData -> {
                _state.update {
                    it.copy(
                        id = event.task.id,
                        name = event.task.name,
                        description = event.task.description,
                        isTimeSpecify = !event.task.dateTime.isEmpty(),
                        taskDate = if (event.task.dateTime.isEmpty()) "" else event.task.dateTime.substring(0, 10),
                        taskTime = if (event.task.dateTime.isEmpty()) "" else event.task.dateTime.substring(11, 16),
                        priority = event.task.priority,
                        category = event.task.category,
                    )
                }
            }

            is EditTaskEvent.OnChangeName -> {
                _state.update {
                    val error = it.errorField.toMutableMap().apply {
                        remove(EditTaskState.RequiredFormField.TASK_NAME)
                        if (event.name.isEmpty() || event.name.isBlank()) {
                            put(
                                EditTaskState.RequiredFormField.TASK_NAME,
                                true
                            )
                        }
                    }
                    it.copy(
                        name = event.name,
                        errorField = error
                    )
                }
            }

            is EditTaskEvent.OnChangeDescription -> {
                _state.update { it.copy(description = event.desc) }
            }

            is EditTaskEvent.OnCheckTimeSpecified -> {
                _state.update { it.copy(
                    isTimeSpecify = event.chk,
                    taskDate = "",
                    taskTime = ""
                ) }
            }

            is EditTaskEvent.OnSelectedDate -> {
                _state.update {
                    val error = it.errorField.toMutableMap().apply {
                        remove(EditTaskState.RequiredFormField.TASK_DATE)
                        if (state.value.isTimeSpecify && event.date.isEmpty()) {
                            put(
                                EditTaskState.RequiredFormField.TASK_DATE,
                                true
                            )
                        }
                    }
                    it.copy(
                        taskDate = event.date,
                        errorField = error
                    )
                }
            }

            is EditTaskEvent.OnSelectedTime -> {
                _state.update {
                    val error = it.errorField.toMutableMap().apply {
                        remove(EditTaskState.RequiredFormField.TASK_TIME)
                        if (state.value.isTimeSpecify && event.time.isEmpty()) {
                            put(
                                EditTaskState.RequiredFormField.TASK_TIME,
                                true
                            )
                        }
                    }
                    it.copy(
                        taskTime = event.time,
                        errorField = error
                    )
                }
            }

            is EditTaskEvent.OnSelectedPriority -> {
                _state.update { it.copy(priority = event.priority) }
            }
            is EditTaskEvent.OnSelectedCategory -> {
                _state.update { it.copy(category = event.category) }
            }

            is EditTaskEvent.OnEditTask -> {
                if (isValidForm()) {
                    viewModelScope.launch {
                        _state.update { it.copy(serviceState = ServiceState.LOADING) }
                        editTask()
                    }

                } else {
                    _state.update { it.copy(serviceState = ServiceState.FAILED) }
                }
            }

            is EditTaskEvent.OnDeleteTask -> {
                viewModelScope.launch {
                    deleteTask(state.value.id!!)
                }
            }
        }
    }

    suspend fun editTask() = coroutineScope {
        val request = EditTaskRequest(
            name = state.value.name,
            description = state.value.description,
            dateTime = if (!state.value.taskDate.isNullOrEmpty() && !state.value.taskTime.isNullOrEmpty()) {
                "${state.value.taskDate}T${state.value.taskTime}:00+07:00"
            } else DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(LocalDate.now().atTime(LocalTime.now().truncatedTo(
                ChronoUnit.SECONDS)).atZone(ZoneId.systemDefault())),
            priority = state.value.priority,
            category = state.value.category
        )
        val response = repository.editTask(state.value.id!!, request)
        when (response) {
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        serviceState = ServiceState.SUCCESS
                    )
                }
            }

            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        serviceState = ServiceState.FAILED
                    )
                }
            }
        }
    }

    suspend fun deleteTask(id: String) = coroutineScope {
        val response = repository.deleteTask(id)
        when (response) {
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        serviceState = ServiceState.SUCCESS
                    )
                }
            }

            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        serviceState = ServiceState.FAILED
                    )
                }
            }
        }
    }

    fun isValidForm(): Boolean {
        val errorField = mutableMapOf<EditTaskState.RequiredFormField, Boolean>()
        if (state.value.name.isNullOrEmpty() || state.value.name.isNullOrBlank()) {
            errorField[EditTaskState.RequiredFormField.TASK_NAME] = true
        }
        if (state.value.isTimeSpecify && state.value.taskDate.isNullOrEmpty()) {
            errorField[EditTaskState.RequiredFormField.TASK_DATE] = true
        }
        if (state.value.isTimeSpecify && state.value.taskTime.isNullOrEmpty()) {
            errorField[EditTaskState.RequiredFormField.TASK_TIME] = true
        }
        _state.update {
            it.copy(
                errorField = errorField
            )
        }
        return errorField.isEmpty()
    }

    fun setIdle() {
        _state.update {
            it.copy(
                serviceState = ServiceState.IDLE
            )
        }
    }
}