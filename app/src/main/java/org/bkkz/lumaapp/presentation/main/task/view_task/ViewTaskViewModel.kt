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
import org.bkkz.lumaapp.data.entity.google_calendar.CalendarEventRequest
import org.bkkz.lumaapp.data.entity.task.EditTaskRequest
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.data.local.UserTaskEntity
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskState
import java.time.LocalDate
import java.time.ZonedDateTime

class ViewTaskViewModel(private val repository: Repository) : ViewModel() {
    private val _state: MutableStateFlow<ViewTaskState> = MutableStateFlow(ViewTaskState())
    val state: StateFlow<ViewTaskState> = _state.asStateFlow()

    fun onEvent(event: ViewTaskEvent) {
        when (event) {
            is ViewTaskEvent.LoadFirstTimeTasks -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    getFirstTimeTasks()
                    _state.update { it.copy(isLoading = false) }
                }
            }

            is ViewTaskEvent.OnUserSelectedMonth -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    getAllMonthlyUserTask(
                        "${event.selectedMonth.year}-${
                            event.selectedMonth.monthValue.toString().padStart(2, '0')
                        }"
                    )
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

    fun currentMonthString() : String { return "${state.value.selectedMonth.year}-${
        state.value.selectedMonth.monthValue.toString().padStart(2, '0')
    }" }

    suspend fun getFirstTimeTasks(){
        val monthlyTaskApi = repository.getAllUserTasks(currentMonthString())
        when (monthlyTaskApi) {
            is ApiResult.Success -> {
                val monthlyTasksResult = monthlyTaskApi.data
                getAllDailyUserTask(state.value.selectedDate)
                _state.update {
                    it.copy(
                        allMonthlyEventsDate = dateContainEvents(monthlyTasksResult),
                        allMonthlyUserTasks = monthlyTasksResult,
                    )
                }
            }

            is ApiResult.Error -> {
                Log.e("ViewTaskViewModel", "Can't get monthly task on initial load")
            }
        }


    }

    suspend fun getAllMonthlyUserTask(date: String){
        var monthlyTasksResult: List<Task>? = null
        val localMonthlyTasks = repository.getLocalUserTaskByDate(date)
        if(localMonthlyTasks.isEmpty()){
            val monthlyTaskApi = repository.getAllUserTasks(date)
            when (monthlyTaskApi) {
                is ApiResult.Success -> {
                    monthlyTasksResult = monthlyTaskApi.data
                    Log.d("ViewTaskViewModel", dateContainEvents(monthlyTasksResult).toString())
                }

                is ApiResult.Error -> {
                    Log.e("ViewTaskViewModel", "Can't get monthly task on $date")
                }
            }
        }else{
            monthlyTasksResult = localMonthlyTasks.map { task ->
                Task(
                    id = task.id,
                    name = task.name,
                    description = task.description ?: "",
                    dateTime = task.dateTime,
                    isFinished = task.isFinished,
                    userId = task.userId,
                    category = task.category,
                    priority = task.priority,
                    isGoogleCalendarTask = task.isGoogleCalendarTask,
                )
            }
        }

        _state.update {
            it.copy(
                allMonthlyEventsDate = dateContainEvents(monthlyTasksResult),
                allMonthlyUserTasks = monthlyTasksResult,
            )
        }
    }

    suspend fun getAllDailyUserTask(date: String){
        var dailyTasksResult: List<Task> = emptyList()
        val dailyLocalTasks = repository.getLocalUserTaskByDate(date)
        if(!dailyLocalTasks.isEmpty()){
            dailyTasksResult = dailyLocalTasks.map { task ->
                Task(
                    id = task.id,
                    name = task.name,
                    description = task.description ?: "",
                    dateTime = task.dateTime,
                    isFinished = task.isFinished,
                    userId = task.userId,
                    category = task.category,
                    priority = task.priority,
                    isGoogleCalendarTask = task.isGoogleCalendarTask,
                )
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

    suspend fun markCompleted(taskId: String, editTaskRequest: EditTaskRequest){
        val response = repository.editTask(taskId, editTaskRequest)
        when (response) {
            is ApiResult.Success -> {
                repository.updateLocalUserTaskStatus(taskId, editTaskRequest.isFinished ?: false)
                getAllMonthlyUserTask(currentMonthString())
                getAllDailyUserTask(state.value.selectedDate)
            }

            is ApiResult.Error -> {}
        }
    }

    fun deleteTask(id: String) = {
        viewModelScope.launch {
            repository.deleteTask(id)
            repository.deleteLocalUserTaskById(id)
        }

    }

    fun insertToGoogleCalendar(oldTask : Task, calendarEventRequest: CalendarEventRequest){
        viewModelScope.launch {
            repository.deleteTask(oldTask.id)
            repository.deleteLocalUserTaskById(oldTask.id)
            val result = repository.insertGoogleCalendarEvent(calendarEventRequest)
            when(result){
                is ApiResult.Success -> {
                    repository.insertLocalUserTask(UserTaskEntity(
                        id = result.data!!,
                        name = oldTask.name,
                        description = oldTask.description,
                        dateTime = oldTask.dateTime,
                        isFinished = oldTask.isFinished,
                        userId = oldTask.userId,
                        category = oldTask.category,
                        priority = oldTask.priority,
                        isGoogleCalendarTask = true
                    ))
                    onEvent(ViewTaskEvent.LoadFirstTimeTasks)
                    Log.d("ViewTaskViewModel", "Successfully inserted to Google Calendar with event ID: ${result.data}")
                }
                is ApiResult.Error -> {
                    Log.e("ViewTaskViewModel", "Error inserting to Google Calendar: ${result.exception}")
                }
            }
        }
    }

}