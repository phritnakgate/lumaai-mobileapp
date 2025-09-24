package org.bkkz.lumaapp.presentation.main.task.view_task

import android.util.Log
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
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskState
import java.time.LocalDate
import java.time.ZonedDateTime

class ViewTaskViewModel(private val repository: Repository) : ViewModel() {
    private val _state : MutableStateFlow<ViewTaskState> = MutableStateFlow(ViewTaskState())
    val state: StateFlow<ViewTaskState> = _state.asStateFlow()

    fun onEvent(event: ViewTaskEvent){
        when(event){
            is ViewTaskEvent.LoadFirstTimeTasks -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    getAllMonthlyUserTask("${state.value.selectedMonth.year}-${state.value.selectedMonth.monthValue.toString().padStart(2,'0')}")
                    getAllDailyUserTask(state.value.selectedDate)
                    _state.update { it.copy(isLoading = false) }
                }
            }
            is ViewTaskEvent.OnUserSelectedMonth -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    getAllMonthlyUserTask("${event.selectedMonth.year}-${event.selectedMonth.monthValue.toString().padStart(2,'0')}")
                    _state.update {
                        it.copy(
                            selectedMonthPosition = event.position,
                            selectedMonth = event.selectedMonth,
                            isLoading = false
                        )
                    }
                }
            }
            is ViewTaskEvent.OnUserSelectedDate -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    getAllDailyUserTask(event.date)
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    suspend fun getAllMonthlyUserTask(date: String) = coroutineScope{
        val monthlyTaskApi = repository.getAllUserTasks(date)
        var monthlyTasksResult: List<Task>? = null

        when(monthlyTaskApi){
            is ApiResult.Success -> {
                monthlyTasksResult = monthlyTaskApi.data
                Log.d("ViewTaskViewModel",dateContainEvents(monthlyTasksResult).toString())
            }
            is ApiResult.Error -> {
                Log.e("ViewTaskViewModel","Can't get monthly task on $date")
            }
        }

        _state.update {
            it.copy(
                allMonthlyEventsDate = dateContainEvents(monthlyTasksResult),
                allMonthlyUserTasks = monthlyTasksResult,
            )
        }
    }
    suspend fun getAllDailyUserTask(date: String) = coroutineScope{
        var dailyTasksResult: List<Task>? = null
        val dailyTaskApi = repository.getAllUserTasks(date)
        when(dailyTaskApi){
            is ApiResult.Success -> {
                dailyTasksResult = dailyTaskApi.data
            }
            is ApiResult.Error -> {
                Log.e("ViewTaskViewModel","Can't get daily task on $date")
            }
        }

        _state.update {
            it.copy(
                selectedDate = date,
                allDailyUserTasks = dailyTasksResult
            )
        }
    }

    private fun dateContainEvents(tasks: List<Task>?): Set<LocalDate> {
        if (tasks.isNullOrEmpty()) {
            return emptySet()
        }
        return tasks
            .map { task ->
                val zonedDateTime = ZonedDateTime.parse(task.dateTime)
                zonedDateTime.toLocalDate()
            }
            .toSet()
    }

    suspend fun markCompleted(taskId : String, editTaskRequest: EditTaskRequest) = coroutineScope{
        val response = repository.editTask(taskId, editTaskRequest)
        when(response){
            is ApiResult.Success -> {
                getAllDailyUserTask(state.value.selectedDate)
                getAllMonthlyUserTask("${state.value.selectedMonth.year}-${state.value.selectedMonth.monthValue.toString().padStart(2,'0')}")
            }
            is ApiResult.Error -> {}
        }
    }
}