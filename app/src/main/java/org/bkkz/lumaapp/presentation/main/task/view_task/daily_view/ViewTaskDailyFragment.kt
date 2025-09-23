package org.bkkz.lumaapp.presentation.main.task.view_task.daily_view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.task.add_task.AddTaskActivity
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskViewModel
import org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.adapter.TaskListAdapter
import org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.calendar.CalendarViewPagerAdapter
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.bkkz.lumaapp.util.mapper.MonthStringMapper
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.YearMonth

class ViewTaskDailyFragment : Fragment() {

    private val baseYm: YearMonth = YearMonth.now()

    //ViewModel
    private val viewModel: ViewTaskViewModel by activityViewModel()

    //UI
    private lateinit var backMonth: ImageView
    private lateinit var txtViewCurrentMonth: TextView
    private lateinit var forwardMonth: ImageView
    private lateinit var viewPagerCalendar: ViewPager2
    private lateinit var addTaskBtn: ConstraintLayout
    private lateinit var recyclerTaskLists: RecyclerView
    private lateinit var imgViewNoTask: ImageView
    private lateinit var txtViewNoTask: TextView

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

    private fun findView() {
        backMonth = requireView().findViewById(R.id.imgview_daily_task_month_back)
        txtViewCurrentMonth = requireView().findViewById(R.id.txtview_daily_task_month)
        forwardMonth = requireView().findViewById(R.id.imgview_daily_task_month_forward)
        viewPagerCalendar = requireView().findViewById(R.id.viewpager_daily_task_calendar)
        recyclerTaskLists = requireView().findViewById(R.id.recyclerview_daily_task)
        addTaskBtn = requireView().findViewById(R.id.constraintlayout_daily_task_add)
        imgViewNoTask = requireView().findViewById(R.id.imgview_daily_task_no_task)
        txtViewNoTask = requireView().findViewById(R.id.txtview_daily_task_no_task)
    }

    private fun setupView() {
        //Adapter for calendar
        val adapter = CalendarViewPagerAdapter(requireActivity())
        viewPagerCalendar.adapter = adapter
        viewPagerCalendar.setCurrentItem(viewModel.state.value.selectedMonthPosition, false)
        viewPagerCalendar.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val ym = yearMonthFor(position)
                viewModel.onEvent(ViewTaskEvent.OnUserSelectedMonth(position, ym))
            }
        })
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                setMonthTitle(state.selectedMonth)
                //Adapter for Task
                recyclerTaskLists.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

                val dailyTasks = state.allDailyUserTasks
                if (dailyTasks.isNullOrEmpty()) {
                    recyclerTaskLists.visibility = View.GONE
                    imgViewNoTask.visibility = View.VISIBLE
                    txtViewNoTask.visibility = View.VISIBLE
                } else {
                    recyclerTaskLists.visibility = View.VISIBLE
                    imgViewNoTask.visibility = View.GONE
                    txtViewNoTask.visibility = View.GONE
                    recyclerTaskLists.adapter = TaskListAdapter(dailyTasks)
                }
            }
        }
    }

    private fun setupEvents() {
        backMonth.setOnClickListener {
            val position = viewPagerCalendar.currentItem - 1
            viewPagerCalendar.currentItem = position
        }
        forwardMonth.setOnClickListener {
            val position = viewPagerCalendar.currentItem + 1
            viewPagerCalendar.currentItem = position
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