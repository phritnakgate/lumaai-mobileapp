package org.bkkz.lumaapp.util

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.calendar.CalendarDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

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
        val layoutManager = GridLayoutManager(requireContext(), 7)
        //TEMP MOCK EVENTS
        val events: List<Date> = listOf(Date())

        calendarAdapter = CalendarAdapter(events, requireContext(), null, currentMonthDate!!) { clickedDay ->
            Toast.makeText(requireContext(), "Clicked on: ${clickedDay.date}", Toast.LENGTH_SHORT).show()
        }

        calendarRecyclerView.layoutManager = layoutManager
        calendarRecyclerView.adapter = calendarAdapter
        calendarAdapter.submitList(loadedDates)
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