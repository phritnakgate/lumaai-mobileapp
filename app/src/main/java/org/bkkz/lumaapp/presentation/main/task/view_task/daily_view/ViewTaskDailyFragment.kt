package org.bkkz.lumaapp.presentation.main.task.view_task.daily_view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.add_task.AddTaskActivity
import org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.adapter.TaskListAdapter
import org.bkkz.lumaapp.util.CalendarViewPagerAdapter
import org.bkkz.lumaapp.util.mapper.MonthStringMapper
import java.time.YearMonth

class ViewTaskDailyFragment : Fragment() {

    //TEMPORARY DATA FOR TESTING CHANGE TO SERVICE INSTEAD\\
    private val mockData: List<Task> = listOf(
        Task(
            id = "-OY9HJ4mDW-BGyoqbWdj",
            name = "ทดสอบ 1",
            description = "ทดสอบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบบ",
            dateTime = "2025-09-01T17:00:00.0615169+07:00",
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
    private val baseYm: YearMonth = YearMonth.now()
    //UI
    private lateinit var backMonth : ImageView
    private lateinit var txtViewCurrentMonth : TextView
    private lateinit var forwardMonth : ImageView
    private lateinit var viewPagerCalendar: ViewPager2
    private lateinit var addTaskBtn : ConstraintLayout
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
        backMonth = requireView().findViewById(R.id.imgview_daily_task_month_back)
        txtViewCurrentMonth = requireView().findViewById(R.id.txtview_daily_task_month)
        forwardMonth = requireView().findViewById(R.id.imgview_daily_task_month_forward)
        viewPagerCalendar = requireView().findViewById(R.id.viewpager_daily_task_calendar)
        recyclerTaskLists = requireView().findViewById(R.id.recyclerview_daily_task)
        addTaskBtn = requireView().findViewById(R.id.constraintlayout_daily_task_add)
        imgViewNoTask = requireView().findViewById(R.id.imgview_daily_task_no_task)
        txtViewNoTask = requireView().findViewById(R.id.txtview_daily_task_no_task)
    }
    private fun setupView(){
        setMonthTitle(baseYm)

        //Adapter for calendar
        val adapter = CalendarViewPagerAdapter(requireActivity())
        viewPagerCalendar.adapter = adapter
        viewPagerCalendar.setCurrentItem(CalendarViewPagerAdapter.START_POSITION, false)
        viewPagerCalendar.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                //TODO: Implement ViewModel when Implement services
                super.onPageSelected(position)
                val ym = yearMonthFor(position)
                setMonthTitle(ym)
            }
        })

        //Adapter for Task
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
        backMonth.setOnClickListener {
            viewPagerCalendar.currentItem = viewPagerCalendar.currentItem - 1
        }
        forwardMonth.setOnClickListener {
            viewPagerCalendar.currentItem = viewPagerCalendar.currentItem + 1
        }
        addTaskBtn.setOnClickListener {
            val intent = Intent(requireContext(), AddTaskActivity::class.java)
            startActivity(intent)
        }
    }

    private fun yearMonthFor(position: Int): YearMonth {
        val diff = position - CalendarViewPagerAdapter.START_POSITION
        return baseYm.plusMonths(diff.toLong())
    }

    private fun setMonthTitle(ym: YearMonth) {
        val resName = "month_${ym.monthValue}_full"
        val monthText = MonthStringMapper.getString(requireContext(), resName)
        txtViewCurrentMonth.text = "$monthText ${ym.year}"
    }

}