package org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.calendar.CalendarDay
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskViewModel
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private val viewModel: ViewTaskViewModel by activityViewModel()

    private lateinit var loadedDates: List<CalendarDay>
    private var currentMonthDate: Date? = null

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupData()
        findView(view)
        setupView()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    private fun setupData() {
        val year = arguments?.getInt(ARG_YEAR) ?: Calendar.getInstance().get(Calendar.YEAR)
        val month = arguments?.getInt(ARG_MONTH) ?: Calendar.getInstance().get(Calendar.MONTH)
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        currentMonthDate = calendar.time
        loadedDates = generateDaysInMonth(currentMonthDate!!)
    }

    private fun findView(view: View) {
        calendarRecyclerView = view.findViewById(R.id.recyclerview_calendar_weeks)
    }

    private fun setupView() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                val layoutManager = GridLayoutManager(requireContext(), 7)
                val events: Set<LocalDate> = state.allMonthlyEventsDate

                calendarAdapter = CalendarAdapter(events, requireContext(), null, currentMonthDate!!) { clickedDay ->
                    val localDate = clickedDay.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    val formattedDate = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    viewModel.onEvent(ViewTaskEvent.OnUserSelectedDate(formattedDate))
                }

                calendarRecyclerView.layoutManager = layoutManager
                calendarRecyclerView.adapter = calendarAdapter
                calendarAdapter.submitList(loadedDates)
            }
        }

    }

    private fun generateDaysInMonth(date: Date): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val monthBeginning = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginning)

        val sdf = SimpleDateFormat("d", Locale.getDefault())

        while (days.size < 42) {
            days.add(CalendarDay(sdf.format(calendar.time), calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }


    companion object {
        private const val ARG_YEAR = "year"
        private const val ARG_MONTH = "month"

        fun newInstance(year: Int, month: Int): CalendarFragment {
            val fragment = CalendarFragment()
            val args = Bundle()
            args.putInt(ARG_YEAR, year)
            args.putInt(ARG_MONTH, month)
            fragment.arguments = args
            return fragment
        }
    }
}