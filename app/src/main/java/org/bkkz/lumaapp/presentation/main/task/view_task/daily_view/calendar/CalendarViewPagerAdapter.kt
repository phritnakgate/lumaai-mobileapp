package org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.calendar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.Calendar

class CalendarViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, position - START_POSITION)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        return CalendarFragment.newInstance(year, month)
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}