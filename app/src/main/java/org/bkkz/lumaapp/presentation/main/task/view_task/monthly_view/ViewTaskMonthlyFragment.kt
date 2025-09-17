package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.adapter.MonthlyViewPagerAdapter
import org.bkkz.lumaapp.util.CalendarViewPagerAdapter
import org.bkkz.lumaapp.util.component.monthly_task_recycler.TimelineItem
import org.bkkz.lumaapp.util.mapper.MonthStringMapper
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class ViewTaskMonthlyFragment : Fragment() {

    //ViewModel
    private val viewModel: ViewTaskMonthlyViewModel by activityViewModels() //Change to koin vm later

    //UI
    private lateinit var backMonth : ImageView
    private lateinit var txtViewCurrentMonth : TextView
    private lateinit var forwardMonth : ImageView
    private lateinit var viewPagerTaskLists : ViewPager2

    //Variable
    private val baseYm: YearMonth = YearMonth.now()

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
        return inflater.inflate(R.layout.fragment_view_task_monthly, container, false)
    }

    private fun findView(){
        backMonth = requireView().findViewById(R.id.imgview_monthly_task_month_back)
        txtViewCurrentMonth = requireView().findViewById(R.id.txtview_monthly_task_month)
        forwardMonth = requireView().findViewById(R.id.imgview_monthly_task_month_forward)
        viewPagerTaskLists = requireView().findViewById(R.id.viewpager_monthly_task)

    }
    private fun setupView(){
        setMonthTitle(baseYm)
        //Adapter for MonthlyTask
        val adapter = MonthlyViewPagerAdapter(requireActivity())
        viewPagerTaskLists.adapter = adapter
        viewPagerTaskLists.setCurrentItem(MonthlyViewPagerAdapter.START_POSITION, false)
        viewPagerTaskLists.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                //TODO: Implement ViewModel when Implement services
                super.onPageSelected(position)
                val ym = yearMonthFor(position)
                setMonthTitle(ym)
                viewModel.loadTasksFor(ym)
            }
        })
        viewModel.loadTasksFor(baseYm)
    }
    private fun setupEvents(){
        backMonth.setOnClickListener {
            viewPagerTaskLists.currentItem = viewPagerTaskLists.currentItem - 1
        }
        forwardMonth.setOnClickListener {
            viewPagerTaskLists.currentItem = viewPagerTaskLists.currentItem + 1
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