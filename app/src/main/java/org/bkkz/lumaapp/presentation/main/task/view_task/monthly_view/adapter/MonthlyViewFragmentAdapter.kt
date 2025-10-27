package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.adapter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.util.DateTime
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.google_calendar.CalendarEventRequest
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.edit_task.EditTaskActivity
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskViewModel
import org.bkkz.lumaapp.util.component.monthly_task_recycler.TimelineItem
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.enums.TaskCategory
import org.bkkz.lumaapp.util.enums.TaskPriority
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MonthlyViewFragmentAdapter(
    private val onPermissionNeeded: (Intent) -> Unit,
    private val viewModel: ViewTaskViewModel
) : ListAdapter<TimelineItem, RecyclerView.ViewHolder>(MonthlyTaskListDiffCallback()) {

    interface OnTaskCheckedListener {
        fun onTaskChecked(item: Task)
    }

    private var onTaskCheckedListener: OnTaskCheckedListener? = null
    fun setOnTaskCheckedListener(listener: OnTaskCheckedListener) {
        this.onTaskCheckedListener = listener
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_BODY = 2
        private const val VIEW_TYPE_FOOTER = 3
    }

    abstract inner class BaseMonthlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskHead: ConstraintLayout =
            itemView.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = itemView.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = itemView.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = itemView.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = itemView.findViewById(R.id.txtview_recycler_task_desc)
        val taskCategory: TextView = itemView.findViewById(R.id.txtview_recycler_task_category)
        val taskPriority: TextView = itemView.findViewById(R.id.txtview_recycler_task_priority)
        val taskEdit: ImageView = itemView.findViewById(R.id.imgview_recycler_task_edit)
        val ggCalendar: ImageView = itemView.findViewById(R.id.imgview_recycler_task_ggcalendar)
        val ggCalendarText: TextView = itemView.findViewById(R.id.txtview_recycler_task_ggcalendar)

        protected fun bindTask(task: Task) {
            val sharedPref = itemView.context.getSharedPreferences("userSession", MODE_PRIVATE)
            val userEmail = sharedPref.getString("googleCalendarEmail", null)

            var isFinished: Boolean = task.isFinished
            if (isFinished) {
                taskHead.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                taskCheck.setImageResource(R.drawable.ic_task_success)
            } else {
                taskHead.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.rect_secondary)
                taskCheck.setImageResource(R.drawable.circ_white)
            }
            taskName.text = task.name
            taskTime.text = OffsetDateTime.parse(task.dateTime)
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            if(task.description.isNotEmpty() && task.description.isNotBlank()){
                taskDesc.text =  task.description
                taskDesc.setTextColor(itemView.context.getColor(R.color.black))
            }else{
                taskDesc.text = itemView.context.getString(R.string.view_task_no_description)
                taskDesc.setTextColor(itemView.context.getColor(R.color.border_color))
            }
            taskEdit.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, EditTaskActivity::class.java)
                intent.putExtra("TASK_DATA", task)
                context.startActivity(intent)
            }
            taskCheck.setOnClickListener {
                isFinished = !isFinished
                onTaskCheckedListener?.onTaskChecked(task)
                if (isFinished) {
                    taskHead.background =
                        ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                    taskCheck.setImageResource(R.drawable.ic_task_success)
                } else {
                    taskHead.background =
                        ContextCompat.getDrawable(itemView.context, R.drawable.rect_secondary)
                    taskCheck.setImageResource(R.drawable.circ_white)
                }
            }
            val category = task.category
            val categoryColorRes = when (category) {
                0 -> R.color.category_color_0
                1 -> R.color.category_color_1
                2 -> R.color.category_color_2
                3 -> R.color.category_color_3
                4 -> R.color.category_color_4
                else -> R.color.category_color_0
            }
            taskCategory.text = TaskCategory.fromInt(category)?.displayName
            taskCategory.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                categoryColorRes
            )
            val priority = task.priority
            val priorityColorRes = when (priority) {
                0 -> R.color.danger
                1 -> R.color.secondary
                2 -> R.color.primary
                else -> R.color.primary
            }
            taskPriority.text = TaskPriority.fromInt(priority)?.displayName
            taskPriority.backgroundTintList = ContextCompat.getColorStateList(
                itemView.context,
                priorityColorRes
            )
            ggCalendar.setOnClickListener {
                if (userEmail == null) {
                    OneActionDialog(itemView.context).show(
                        drawable = R.drawable.ic_dialog_no,
                        title = "Error",
                        message = "Please connect to Google Calendar first!",
                        onConfirmClickListener = {}
                    )
                    return@setOnClickListener
                }
                createGoogleCalendarEvent(itemView.context, userEmail, task)
            }
            if (task.isGoogleCalendarTask) {
                ggCalendarText.visibility = View.GONE
            } else {
                ggCalendarText.visibility = View.VISIBLE
            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : BaseMonthlyViewHolder(itemView) {
        private val dateTextView: TextView =
            itemView.findViewById(R.id.txtview_monthly_recycler_head_date)
        private val dayTextView: TextView =
            itemView.findViewById(R.id.txtview_monthly_recycler_head_day)

        fun bind(header: TimelineItem.TaskHeader) {
            dateTextView.text = header.date
            dayTextView.text = header.day

            bindTask(header.task)
        }
    }

    inner class TaskBodyViewHolder(itemView: View) : BaseMonthlyViewHolder(itemView) {
        fun bind(data: TimelineItem.TaskBody) {
            bindTask(data.task)
        }
    }

    inner class TaskFooterViewHolder(itemView: View) : BaseMonthlyViewHolder(itemView) {
        fun bind(data: TimelineItem.TaskFooter) {
            bindTask(data.task)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineItem.TaskHeader -> VIEW_TYPE_HEADER
            is TimelineItem.TaskBody -> VIEW_TYPE_BODY
            is TimelineItem.TaskFooter -> VIEW_TYPE_FOOTER
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_task_monthly_header, parent, false)
                HeaderViewHolder(view)
            }

            VIEW_TYPE_BODY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_task_monthly_body, parent, false)
                TaskBodyViewHolder(view)
            }

            VIEW_TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_task_monthly_footer, parent, false)
                TaskFooterViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is HeaderViewHolder -> {
                holder.bind(getItem(position) as TimelineItem.TaskHeader)
            }

            is TaskBodyViewHolder -> {
                holder.bind(getItem(position) as TimelineItem.TaskBody)
            }

            is TaskFooterViewHolder -> {
                holder.bind(getItem(position) as TimelineItem.TaskFooter)
            }
        }
    }

    private fun createGoogleCalendarEvent(context: Context, userEmail: String?, task: Task) {
        if (userEmail == null) {
            OneActionDialog(context).show(
                drawable = R.drawable.ic_dialog_no,
                title = "Error",
                message = "Please connect to Google Calendar first!",
                onConfirmClickListener = {}
            )
            return
        }
        try {
            val dateString = task.dateTime
            val utc = TimeZone.getTimeZone("UTC")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            dateFormat.timeZone = utc

            val taskDateJava: Date = dateFormat.parse(dateString.substring(0, 10))!!
            val startDate = DateTime(true, taskDateJava.time, 0)

            val calendar = java.util.Calendar.getInstance(utc)
            calendar.time = taskDateJava
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val endDate = DateTime(true, calendar.time.time, 0)

            val calendarEventRequest = CalendarEventRequest(
                name = task.name,
                description = task.description,
                startTime = startDate.toStringRfc3339(),
                endTime = endDate.toStringRfc3339(),
                ownerEmail = userEmail,
            )

            Log.i(
                "TaskListAdapter",
                "Event Name: ${calendarEventRequest.name}\nEvent Desc: ${calendarEventRequest.description}\nEvent Date: ${calendarEventRequest.startTime} ==> ${calendarEventRequest.endTime}"
            )

            viewModel.insertToGoogleCalendar(task, calendarEventRequest)
            OneActionDialog(context).show(
                drawable = R.drawable.ic_dialog_success,
                title = "Add to calendar Success!",
                message = "",
                onConfirmClickListener = {
                }
            )

        } catch (e: UserRecoverableAuthIOException) {
            onPermissionNeeded(e.intent)
        } catch (e: Exception) {
            Log.d("TaskListAdapter", e.message.toString())
            OneActionDialog(context).show(
                drawable = R.drawable.ic_dialog_no,
                title = "Error",
                message = e.message.toString(),
                onConfirmClickListener = {}
            )
        }
    }


    class MonthlyTaskListDiffCallback : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return when (oldItem) {
                is TimelineItem.TaskHeader -> oldItem.task.id == (newItem as TimelineItem.TaskHeader).task.id
                is TimelineItem.TaskBody -> oldItem.task.id == (newItem as TimelineItem.TaskBody).task.id
                is TimelineItem.TaskFooter -> oldItem.task.id == (newItem as TimelineItem.TaskFooter).task.id
            }
        }

        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }
    }

}