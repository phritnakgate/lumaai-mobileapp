package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.task.add_task.AddTaskActivity
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskViewModel
import org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.calendar.CalendarViewPagerAdapter
import org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.adapter.MonthlyViewPagerAdapter
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.isConnectedToInternet
import org.bkkz.lumaapp.util.mapper.MonthStringMapper
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.time.YearMonth
import java.util.Locale

class ViewTaskMonthlyFragment : Fragment() {

    //ViewModel
    private val viewModel: ViewTaskViewModel by activityViewModel()

    //UI
    private lateinit var backMonth: ImageView
    private lateinit var txtViewCurrentMonth: TextView
    private lateinit var forwardMonth: ImageView
    private lateinit var addTaskBtn: ConstraintLayout
    private lateinit var viewPagerTaskLists: ViewPager2

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

    private fun findView() {
        backMonth = requireView().findViewById(R.id.imgview_monthly_task_month_back)
        txtViewCurrentMonth = requireView().findViewById(R.id.txtview_monthly_task_month)
        forwardMonth = requireView().findViewById(R.id.imgview_monthly_task_month_forward)
        addTaskBtn = requireView().findViewById(R.id.constraintlayout_monthly_task_add)
        viewPagerTaskLists = requireView().findViewById(R.id.viewpager_monthly_task)

    }

    private fun setupView() {
        //Adapter for MonthlyTask
        val adapter = MonthlyViewPagerAdapter(requireActivity())
        viewPagerTaskLists.adapter = adapter
        viewPagerTaskLists.setCurrentItem(
            viewModel.state.value.selectedMonthPosition,
            false
        )
        viewPagerTaskLists.registerOnPageChangeCallback(object :
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
            }
        }
    }

    private fun setupEvents() {
        backMonth.setOnClickListener {
            if(viewModel.state.value.isLoading) return@setOnClickListener
            if(!requireActivity().isConnectedToInternet()){
                OneActionDialog(requireContext()).show(
                    drawable = R.drawable.ic_dialog_no,
                    title = requireContext().getString(R.string.no_internet_title),
                    message = requireContext().getString(R.string.no_internet_desc),
                )
                return@setOnClickListener
            }
            viewPagerTaskLists.currentItem = viewModel.state.value.selectedMonthPosition - 1
        }
        forwardMonth.setOnClickListener {
            if(viewModel.state.value.isLoading) return@setOnClickListener
            if(!requireActivity().isConnectedToInternet()){
                OneActionDialog(requireContext()).show(
                    drawable = R.drawable.ic_dialog_no,
                    title = requireContext().getString(R.string.no_internet_title),
                    message = requireContext().getString(R.string.no_internet_desc),
                )
                return@setOnClickListener
            }
            viewPagerTaskLists.currentItem = viewModel.state.value.selectedMonthPosition + 1
        }
        addTaskBtn.setOnClickListener {
            val intent = Intent(requireActivity(), AddTaskActivity::class.java)
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
        val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: Locale.getDefault().language
        val yearText = if(currentLang == "th") ym.year + 543 else ym.year
        txtViewCurrentMonth.text = "$monthText $yearText"
    }

}