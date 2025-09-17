package org.bkkz.lumaapp.util.component.monthly_task_recycler

import org.bkkz.lumaapp.data.entity.task.Task

sealed class TimelineItem {
    data class TaskHeader(val date: String, val day: String, val task: Task) : TimelineItem()
    data class TaskBody(val task: Task) : TimelineItem()
    data class TaskFooter(val task: Task) : TimelineItem()
}