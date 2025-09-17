package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.MonthlyTasksFragment
import java.util.Calendar

class MonthlyViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, position - START_POSITION)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        return MonthlyTasksFragment.Companion.newInstance(year, month)
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}