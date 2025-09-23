package org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.calendar

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.calendar.CalendarDay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class CalendarAdapter(
    val events: Set<LocalDate>,
    val context: Context,
    initialSelectedDate: Date?,
    val currentMonth: Date,
    val onItemClick: (CalendarDay) -> Unit
) : ListAdapter<CalendarDay, CalendarAdapter.ViewHolder>(CalendarDiffCallBack()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date : TextView = view.findViewById(R.id.txtview_calendar_cell)
        val dateCell : ConstraintLayout = view.findViewById(R.id.constraintlayout_calendar_cell)
        val eventLine : View = view.findViewById(R.id.view_calendar_cell_has_event)
    }

    var selectedDate: Date? = initialSelectedDate
        private set

    private var selectedPos: Int? = null

    fun areDatesEqual(dateFirst: Date, dateSecond: Date): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(dateFirst).equals(sdf.format(dateSecond))
    }

    fun isInTheSelectedMonth(dateFirst: Date, dateSecond: Date): Boolean {
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        return sdf.format(dateFirst).equals(sdf.format(dateSecond))
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calendarDay = getItem(position)
        holder.date.text = calendarDay.dayOfMonth

        if (isInTheSelectedMonth(calendarDay.date, currentMonth)) {
            holder.date.setTextColor(context.getColor(R.color.black))
            val isSelected = selectedDate?.let { areDatesEqual(it, calendarDay.date) } == true
            val isToday = areDatesEqual(Date(), calendarDay.date)
            when {
                isToday -> {
                    holder.dateCell.setBackgroundResource(R.drawable.circ_primary)
                    holder.date.setTextColor(context.getColor(R.color.white))
                }
                isSelected -> {
                    holder.dateCell.setBackgroundResource(R.drawable.circ_secondary)
                    holder.date.setTextColor(context.getColor(R.color.white))
                }
                else -> {
                    holder.dateCell.setBackgroundResource(R.drawable.circ_white)
                    holder.date.setTextColor(context.getColor(R.color.black))
                }
            }
            holder.dateCell.setOnClickListener {
                val prevPos = selectedPos
                selectedDate = calendarDay.date
                selectedPos = holder.bindingAdapterPosition
                prevPos?.let { notifyItemChanged(it) }
                selectedPos?.let { notifyItemChanged(it) }

                onItemClick(calendarDay)
            }
        }else{
            holder.date.setTextColor(context.getColor(R.color.disabled))
            holder.dateCell.setBackgroundResource(android.R.color.transparent)
            holder.dateCell.setOnClickListener(null)
        }
        val dayLocalDate = calendarDay.date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val hasEvent = events.contains(dayLocalDate)
        //Log.i("CalendarAdapter","Generated: $dayLocalDate | hasEvent: $hasEvent\nEvents : $events")
        holder.eventLine.visibility = if (hasEvent) View.VISIBLE else View.GONE
    }

}

class CalendarDiffCallBack() : DiffUtil.ItemCallback<CalendarDay>() {
    override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
        return oldItem == newItem
    }

}