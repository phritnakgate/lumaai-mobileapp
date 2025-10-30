package org.bkkz.lumaapp.presentation.main.task.add_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.entity.task.CreateTaskRequest
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.task.add_task.state.AddTaskEvent
import org.bkkz.lumaapp.presentation.main.task.add_task.state.AddTaskState
import org.bkkz.lumaapp.util.enums.ServiceState

class AddTaskViewModel(private val repository: Repository) : ViewModel() {
    private val _state: MutableStateFlow<AddTaskState> = MutableStateFlow(AddTaskState())
    val state: StateFlow<AddTaskState> = _state.asStateFlow()

    fun onEvent(event: AddTaskEvent) {
        when (event) {
            is AddTaskEvent.OnChangeName -> {
                _state.update {
                    val error = it.errorField.toMutableMap().apply {
                        remove(AddTaskState.RequiredFormField.TASK_NAME)
                        if (event.name.isEmpty() || event.name.isBlank()) {
                            put(
                                AddTaskState.RequiredFormField.TASK_NAME,
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

            is AddTaskEvent.OnChangeDescription -> {
                _state.update { it.copy(description = event.desc) }
            }

            is AddTaskEvent.OnCheckTimeSpecified -> {
                _state.update { it.copy(isTimeSpecify = event.chk) }
            }

            is AddTaskEvent.OnSelectedDate -> {
                _state.update {
                    val error = it.errorField.toMutableMap().apply {
                        remove(AddTaskState.RequiredFormField.TASK_DATE)
                        if (state.value.isTimeSpecify && event.date.isEmpty()) {
                            put(
                                AddTaskState.RequiredFormField.TASK_DATE,
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

            is AddTaskEvent.OnSelectedTime -> {
                _state.update {
                    val error = it.errorField.toMutableMap().apply {
                        remove(AddTaskState.RequiredFormField.TASK_TIME)
                        if (state.value.isTimeSpecify && event.time.isEmpty()) {
                            put(
                                AddTaskState.RequiredFormField.TASK_TIME,
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

            is AddTaskEvent.OnSelectedCategory -> {
                _state.update { it.copy(category = event.category) }
            }
            is AddTaskEvent.OnSelectedPriority -> {
                _state.update { it.copy(priority = event.priority) }
            }

            is AddTaskEvent.OnCreateTask -> {

                if (isValidForm()) {
                    viewModelScope.launch {
                        _state.update { it.copy(serviceState = ServiceState.LOADING) }
                        createTask()
                    }

                } else {
                    _state.update { it.copy(serviceState = ServiceState.FAILED) }
                }


            }
        }
    }

    suspend fun createTask() = coroutineScope {
        val requestBody = CreateTaskRequest(
            name = state.value.name ?: "",
            description = state.value.description ?: "",
            dueDate = state.value.taskDate ?: "",
            dueTime = state.value.taskTime ?: "",
            category = state.value.category,
            priority = state.value.priority
        )
        val response = repository.createTask(requestBody)
        when (response) {
            is ApiResult.Success -> {
                _state.update { it.copy(serviceState = ServiceState.SUCCESS) }
            }

            is ApiResult.Error -> {
                _state.update { it.copy(serviceState = ServiceState.FAILED, serviceMessage = response.exception.message) }
            }
        }
    }

    fun isValidForm(): Boolean {
        val errorField = mutableMapOf<AddTaskState.RequiredFormField, Boolean>()
        if (state.value.name.isNullOrEmpty() || state.value.name.isNullOrBlank()) {
            errorField[AddTaskState.RequiredFormField.TASK_NAME] = true
        }
        if (state.value.isTimeSpecify && state.value.taskDate.isNullOrEmpty()) {
            errorField[AddTaskState.RequiredFormField.TASK_DATE] = true
        }
        if (state.value.isTimeSpecify && state.value.taskTime.isNullOrEmpty()) {
            errorField[AddTaskState.RequiredFormField.TASK_TIME] = true
        }
        _state.update {
            it.copy(
                errorField = errorField
            )
        }
        return errorField.isEmpty()
    }

    fun setIdle(){
        _state.update {
            it.copy(
                serviceState = ServiceState.IDLE
            )
        }
    }

}