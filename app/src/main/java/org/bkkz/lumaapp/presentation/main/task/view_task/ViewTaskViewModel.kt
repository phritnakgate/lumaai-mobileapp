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
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskState

class ViewTaskViewModel(private val repository: Repository) : ViewModel() {
    private val _state : MutableStateFlow<ViewTaskState> = MutableStateFlow(ViewTaskState())
    val state: StateFlow<ViewTaskState> = _state.asStateFlow()

    fun onEvent(event: ViewTaskEvent){
        when(event){
            is ViewTaskEvent.OnUserSelectedMonth -> {
                _state.update {
                    it.copy(
                        selectedMonthPosition = event.position
                    )
                }
            }
            is ViewTaskEvent.OnUserSelectedDate -> {
                viewModelScope.launch {
                    getAllUserTask(event.date)
                }
            }
        }
    }

    suspend fun getAllUserTask(date: String) = coroutineScope{
        val monthlyTaskApi = repository.getAllUserTasks(date.substring(0,7))
        var monthlyTasksResult: List<Task>? = null

        when(monthlyTaskApi){
            is ApiResult.Success -> {
                monthlyTasksResult = monthlyTaskApi.data
            }
            is ApiResult.Error -> {
                Log.e("ViewTaskViewModel","Can't get monthly task on $date")
            }
        }
        var dailyTasksResult: List<Task>? = null
        if(date.length > 7){
            val dailyTaskApi = repository.getAllUserTasks(date)
            when(dailyTaskApi){
                is ApiResult.Success -> {
                    dailyTasksResult = dailyTaskApi.data
                }
                is ApiResult.Error -> {
                    Log.e("ViewTaskViewModel","Can't get daily task on $date")
                }
            }
        }

        _state.update {
            it.copy(
                allMonthlyUserTasks = monthlyTasksResult,
                allDailyUserTasks = dailyTasksResult
            )
        }
    }
}