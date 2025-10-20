package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.EditTaskRequest
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskViewModel
import org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.adapter.MonthlyViewFragmentAdapter
import org.bkkz.lumaapp.util.component.monthly_task_recycler.TimelineItem
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class MonthlyTasksFragment : Fragment(), MonthlyViewFragmentAdapter.OnTaskCheckedListener {

    //ViewModel
    private val viewModel: ViewTaskViewModel by activityViewModel()

    //UI
    private lateinit var recyclerTaskLists : RecyclerView
    private lateinit var imgViewNoTask : ImageView
    private lateinit var txtViewNotask : TextView

    private val requestCalendarPermissionForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK) {
            //TODO
        } else {
            //TODO
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView()
        setupView()
        setupEvents()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monthly_tasks, container, false)
    }

    private fun findView(){
        recyclerTaskLists = requireView().findViewById(R.id.recyclerview_monthly_task)
        imgViewNoTask = requireView().findViewById(R.id.imgview_monthly_task_no_task)
        txtViewNotask = requireView().findViewById(R.id.txtview_monthly_task_no_task)
    }
    private fun setupView(){

        val adapter = MonthlyViewFragmentAdapter(onPermissionNeeded = {requestCalendarPermissionForResult.launch(it)}, viewModel)
        adapter.setOnTaskCheckedListener(this@MonthlyTasksFragment)
        recyclerTaskLists.adapter = adapter
        recyclerTaskLists.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL, false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                val tasks = state.allMonthlyUserTasks
                if (tasks.isNullOrEmpty()) {
                    recyclerTaskLists.visibility = View.GONE
                    imgViewNoTask.visibility = View.VISIBLE
                    txtViewNotask.visibility = View.VISIBLE
                } else {
                    recyclerTaskLists.visibility = View.VISIBLE
                    imgViewNoTask.visibility = View.GONE
                    txtViewNotask.visibility = View.GONE

                    val timelineItems = prepareTimelineData(tasks)
                    adapter.submitList(timelineItems)
                }
            }
        }
    }
    private fun setupEvents(){}


    private fun prepareTimelineData(tasks: List<Task>): List<TimelineItem> {
        val timelineItems = mutableListOf<TimelineItem>()
        val groupedTasks = tasks.sortedBy { it.dateTime }.groupBy {
            OffsetDateTime.parse(it.dateTime).toLocalDate()
        }
        val dayFormatter = DateTimeFormatter.ofPattern("dd")
        val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E")


        groupedTasks.forEach { (date, tasksOnDate) ->
            val dateString = date.format(dayFormatter)
            val dayString = date.format(dayOfWeekFormatter)
            tasksOnDate.forEachIndexed { index, task ->
                when (index) {
                    0 -> timelineItems.add(TimelineItem.TaskHeader(dateString, dayString, task))
                    tasksOnDate.size - 1 -> timelineItems.add(TimelineItem.TaskFooter(task))
                    else -> timelineItems.add(TimelineItem.TaskBody(task))
                }
            }
        }
        return timelineItems
    }

    override fun onTaskChecked(item: Task) {
        val id = item.id
        val newCheck = EditTaskRequest(isFinished = !item.isFinished)
        lifecycleScope.launch {
            viewModel.markCompleted(id,newCheck)
        }
    }

    companion object {
        private const val ARG_YEAR = "year"
        private const val ARG_MONTH = "month"

        fun newInstance(year: Int, month: Int): MonthlyTasksFragment {
            val fragment = MonthlyTasksFragment()
            val args = Bundle()
            args.putInt(ARG_YEAR, year)
            args.putInt(ARG_MONTH, month)
            fragment.arguments = args
            return fragment
        }
    }
}