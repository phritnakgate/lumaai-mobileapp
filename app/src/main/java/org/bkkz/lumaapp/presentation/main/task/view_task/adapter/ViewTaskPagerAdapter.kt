package org.bkkz.lumaapp.presentation.main.task.view_task.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.ViewTaskDailyFragment
import org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.ViewTaskMonthlyFragment

private const val NUM_TABS = 2

class ViewTaskPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity){
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ViewTaskDailyFragment()
            1 -> ViewTaskMonthlyFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }

    override fun getItemCount(): Int = NUM_TABS

}