package org.bkkz.lumaapp.presentation.main.task.view_task.daily_view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.view_task.adapter.TaskListAdapter

class ViewTaskDailyFragment : Fragment() {

    //TEMPORARY DATA FOR TESTING CHANGE TO ENTITY INSTEAD\\
    private val mockData: List<Task> = listOf(
        Task(
            id = "-OY9HJ4mDW-BGyoqbWdj",
            name = "ทดสอบ 1",
            description = "ทดสอบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบ",
            dateTime = "2025-09-01T17:00:00+07:00",
            isFinished = true,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        ),
        Task(
            id = "-OY9HgBvvgtwgvOV4sEK",
            name = "Task with only Date",
            description = "",
            dateTime = "2025-09-01T08:41:40.0615169+07:00",
            isFinished = false,
            userId = "532QI5E8sJdgzMo4ao0k4ucqyi03"
        )
    )
    private val mockEmptyData: List<Task> = listOf()

    private lateinit var recyclerTaskLists : RecyclerView
    private lateinit var imgViewNoTask : ImageView
    private lateinit var txtViewNoTask : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_view_task_daily, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView()
        setupView()
        setupEvents()
    }

    private fun findView(){
        recyclerTaskLists = requireView().findViewById(R.id.recyclerview_daily_task)
        imgViewNoTask = requireView().findViewById(R.id.imgview_daily_task_no_task)
        txtViewNoTask = requireView().findViewById(R.id.txtview_daily_task_no_task)
    }
    private fun setupView(){
        recyclerTaskLists.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerTaskLists.adapter = TaskListAdapter(mockData)
        if(recyclerTaskLists.adapter?.itemCount == 0){
            recyclerTaskLists.visibility = View.GONE
            imgViewNoTask.visibility = View.VISIBLE
            txtViewNoTask.visibility = View.VISIBLE
        }
    }
    private fun setupEvents(){

    }

}