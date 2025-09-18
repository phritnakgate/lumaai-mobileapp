package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.bkkz.lumaapp.data.entity.task.Task
import java.time.OffsetDateTime
import java.time.YearMonth


class ViewTaskMonthlyViewModel : ViewModel() {
    private val allTasks = listOf(
        Task(
            id = "-OY9HJ4mDW-BGyoqbWdj", name = "ทดสอบเดือนกันยายน 1-1",
            description = "ทดสอบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบ",
            dateTime = "2025-09-01T17:00:00.0615169+07:00", isFinished = true,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        ),
        Task(
            id = "-OY9HJ4mDW-BGyoqbWdj", name = "ทดสอบเดือนกันยายน 1-2",
            description = "ทดสอบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบ",
            dateTime = "2025-09-01T17:00:00.0615169+07:00", isFinished = false,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        ),
        Task(
            id = "-OY9HJ4mDW-BGyoqbWdj", name = "ทดสอบเดือนกันยายน 1-3",
            description = "ทดสอบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบ",
            dateTime = "2025-09-01T17:00:00.0615169+07:00", isFinished = false,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        ),
        Task(
            id = "-OY9HgBvvgtwgvOV4sEK", name = "ประชุมทีมเดือนกันยายน",
            description = "", dateTime = "2025-09-15T09:00:00.0615169+07:00", isFinished = false,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        ),
        Task(
            id = "-anotherId1", name = "จ่ายบิลเดือนสิงหาคม",
            description = "ค่าโทรศัพท์", dateTime = "2025-08-25T11:00:00.0615169+07:00", isFinished = true,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        ),
        Task(
            id = "-anotherId2", name = "ดูหนังเดือนตุลาคม",
            description = "เรื่องใหม่", dateTime = "2025-10-10T20:00:00.0615169+07:00", isFinished = false,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        )
    )

    //Change to StateFlow Later
    private val _state = MutableLiveData<List<Task>>()
    val state: LiveData<List<Task>> = _state

    fun loadTasksFor(yearMonth: YearMonth) {
        val filteredTasks = allTasks.filter { task ->
            val odt = OffsetDateTime.parse(task.dateTime)
            YearMonth.from(odt) == yearMonth
        }
        _state.value = filteredTasks
    }
}