package org.bkkz.lumaapp.presentation.main.task.view_task.state

import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.calendar.CalendarViewPagerAdapter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date
import java.util.Locale

data class ViewTaskState(
    val isLoading : Boolean = false,
    val selectedMonthPosition : Int = CalendarViewPagerAdapter.START_POSITION,
    val selectedMonth : YearMonth = YearMonth.now(),
    val selectedDate : String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val allMonthlyUserTasks : List<Task>? = null,
    val allDailyUserTasks : List<Task>? = null,
    val allMonthlyEventsDate : Set<LocalDate> = emptySet()
)
