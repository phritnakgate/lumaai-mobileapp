package org.bkkz.lumaapp.presentation.main.task.view_task.state

import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.calendar.CalendarViewPagerAdapter

data class ViewTaskState(
    val selectedMonthPosition : Int = CalendarViewPagerAdapter.START_POSITION,
    val allMonthlyUserTasks : List<Task>? = null,
    val allDailyUserTasks : List<Task>? = null
)
