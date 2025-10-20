package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.adapter

import android.accounts.Account
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
import androidx.recyclerview.widget.RecyclerView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.edit_task.EditTaskActivity
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskViewModel
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.bkkz.lumaapp.util.component.monthly_task_recycler.TimelineItem
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.enums.TaskCategory
import org.bkkz.lumaapp.util.enums.TaskPriority
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class MonthlyViewFragmentAdapter(
    private val items: List<TimelineItem>,
    private val onPermissionNeeded: (Intent) -> Unit,
    private val viewModel : ViewTaskViewModel
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    interface OnTaskCheckedListener{
        fun onTaskChecked(item: Task)
    }

    private var onTaskCheckedListener : OnTaskCheckedListener? = null
    fun setOnTaskCheckedListener(listener: OnTaskCheckedListener){
        this.onTaskCheckedListener = listener
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_BODY = 2
        private const val VIEW_TYPE_FOOTER = 3
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.txtview_monthly_recycler_head_date)
        private val dayTextView: TextView = itemView.findViewById(R.id.txtview_monthly_recycler_head_day)
        val taskHead: ConstraintLayout = itemView.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = itemView.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = itemView.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = itemView.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = itemView.findViewById(R.id.txtview_recycler_task_desc)
        val taskCategory: TextView = itemView.findViewById(R.id.txtview_recycler_task_category)
        val taskPriority: TextView = itemView.findViewById(R.id.txtview_recycler_task_priority)
        val taskEdit : ImageView = itemView.findViewById(R.id.imgview_recycler_task_edit)
        val ggCalendar: ImageView = itemView.findViewById(R.id.imgview_recycler_task_ggcalendar)
        val ggCalendarText : TextView = itemView.findViewById(R.id.txtview_recycler_task_ggcalendar)
        fun bind(header: TimelineItem.TaskHeader) {
            val sharedPref = itemView.context.getSharedPreferences("userSession", MODE_PRIVATE)
            val userEmail = sharedPref.getString("googleCalendarEmail", null)

            var isFinished : Boolean = header.task.isFinished
            dateTextView.text = header.date
            dayTextView.text = header.day
            if(isFinished){
                taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                taskCheck.setImageResource(R.drawable.ic_task_success)
            }
            taskName.text = header.task.name
            taskTime.text = OffsetDateTime.parse(header.task.dateTime).format(DateTimeFormatter.ofPattern("HH:mm"))
            taskDesc.text = header.task.description
            taskEdit.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, EditTaskActivity::class.java)
                intent.putExtra("TASK_DATA", header.task)
                context.startActivity(intent)
            }
            taskCheck.setOnClickListener {
                isFinished = !isFinished
                onTaskCheckedListener?.onTaskChecked(header.task)
                if(isFinished){
                    taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                    taskCheck.setImageResource(R.drawable.ic_task_success)
                }else{
                    taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_secondary)
                    taskCheck.setImageResource(R.drawable.circ_white)
                }
            }
            val category = header.task.category
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
            val priority = header.task.priority
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
                if(userEmail == null){
                    OneActionDialog(itemView.context).show(
                        drawable = R.drawable.ic_dialog_no,
                        title = "Error",
                        message = "Please connect to Google Calendar first!",
                        onConfirmClickListener = {}
                    )
                    return@setOnClickListener
                }
                createGoogleCalendarEvent(itemView.context, userEmail, header.task)
            }
            if(header.task.isGoogleCalendarTask){
                ggCalendarText.visibility = View.GONE
            }
        }
    }

    inner class TaskBodyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskHead: ConstraintLayout = itemView.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = itemView.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = itemView.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = itemView.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = itemView.findViewById(R.id.txtview_recycler_task_desc)
        val taskCategory: TextView = itemView.findViewById(R.id.txtview_recycler_task_category)
        val taskPriority: TextView = itemView.findViewById(R.id.txtview_recycler_task_priority)
        val taskEdit : ImageView = itemView.findViewById(R.id.imgview_recycler_task_edit)
        val ggCalendar: ImageView = itemView.findViewById(R.id.imgview_recycler_task_ggcalendar)
        val ggCalendarText : TextView = itemView.findViewById(R.id.txtview_recycler_task_ggcalendar)
        fun bind(data: TimelineItem.TaskBody) {
            val sharedPref = itemView.context.getSharedPreferences("userSession", MODE_PRIVATE)
            val userEmail = sharedPref.getString("googleCalendarEmail", null)

            var isFinished : Boolean = data.task.isFinished
            if(isFinished){
                taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                taskCheck.setImageResource(R.drawable.ic_task_success)
            }
            taskName.text = data.task.name
            taskTime.text = OffsetDateTime.parse(data.task.dateTime).format(DateTimeFormatter.ofPattern("HH:mm"))
            taskDesc.text = data.task.description
            taskEdit.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, EditTaskActivity::class.java)
                intent.putExtra("TASK_DATA", data.task)
                context.startActivity(intent)
            }
            taskCheck.setOnClickListener {
                isFinished = !isFinished
                onTaskCheckedListener?.onTaskChecked(data.task)
                if(isFinished){
                    taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                    taskCheck.setImageResource(R.drawable.ic_task_success)
                }else{
                    taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_secondary)
                    taskCheck.setImageResource(R.drawable.circ_white)
                }
            }
            ggCalendar.setOnClickListener {
                if(userEmail == null){
                    OneActionDialog(itemView.context).show(
                        drawable = R.drawable.ic_dialog_no,
                        title = "Error",
                        message = "Please connect to Google Calendar first!",
                        onConfirmClickListener = {}
                    )
                    return@setOnClickListener
                }
                createGoogleCalendarEvent(itemView.context, userEmail, data.task)
            }
            val category = data.task.category
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
            val priority = data.task.priority
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
            if(data.task.isGoogleCalendarTask){
                ggCalendarText.visibility = View.GONE
            }
        }
    }
    inner class TaskFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val taskHead: ConstraintLayout = itemView.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = itemView.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = itemView.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = itemView.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = itemView.findViewById(R.id.txtview_recycler_task_desc)
        val taskCategory: TextView = itemView.findViewById(R.id.txtview_recycler_task_category)
        val taskPriority: TextView = itemView.findViewById(R.id.txtview_recycler_task_priority)
        val taskEdit : ImageView = itemView.findViewById(R.id.imgview_recycler_task_edit)
        val ggCalendar: ImageView = itemView.findViewById(R.id.imgview_recycler_task_ggcalendar)
        val ggCalendarText : TextView = itemView.findViewById(R.id.txtview_recycler_task_ggcalendar)
        fun bind(data: TimelineItem.TaskFooter) {
            val sharedPref = itemView.context.getSharedPreferences("userSession", MODE_PRIVATE)
            val userEmail = sharedPref.getString("googleCalendarEmail", null)

            var isFinished : Boolean = data.task.isFinished
            if(isFinished){
                taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                taskCheck.setImageResource(R.drawable.ic_task_success)
            }
            taskName.text = data.task.name
            taskTime.text = OffsetDateTime.parse(data.task.dateTime).format(DateTimeFormatter.ofPattern("HH:mm"))
            taskDesc.text = data.task.description
            taskEdit.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, EditTaskActivity::class.java)
                intent.putExtra("TASK_DATA", data.task)
                context.startActivity(intent)
            }
            taskCheck.setOnClickListener {
                isFinished = !isFinished
                onTaskCheckedListener?.onTaskChecked(data.task)
                if(isFinished){
                    taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_disabled_color)
                    taskCheck.setImageResource(R.drawable.ic_task_success)
                }else{
                    taskHead.background = ContextCompat.getDrawable(itemView.context, R.drawable.rect_secondary)
                    taskCheck.setImageResource(R.drawable.circ_white)
                }
            }
            val category = data.task.category
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
            val priority = data.task.priority
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
                if(userEmail == null){
                    OneActionDialog(itemView.context).show(
                        drawable = R.drawable.ic_dialog_no,
                        title = "Error",
                        message = "Please connect to Google Calendar first!",
                        onConfirmClickListener = {}
                    )
                    return@setOnClickListener
                }
                createGoogleCalendarEvent(itemView.context, userEmail, data.task)
            }
            if(data.task.isGoogleCalendarTask){
                ggCalendarText.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
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
                holder.bind(items[position] as TimelineItem.TaskHeader)
            }
            is TaskBodyViewHolder -> {
                holder.bind(items[position] as TimelineItem.TaskBody)
            }
            is TaskFooterViewHolder -> {
                holder.bind(items[position] as TimelineItem.TaskFooter)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun createGoogleCalendarEvent(context: Context,userEmail : String?, task : Task){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mCredential = GoogleAccountCredential.usingOAuth2(
                    context,
                    arrayListOf(CalendarScopes.CALENDAR)
                ).setBackOff(ExponentialBackOff()).apply {
                    selectedAccount = userEmail?.let { Account(it, "com.google") }
                }
                Log.d(
                    "TaskListAdapter",
                    "Calendar credential account: ${mCredential?.selectedAccountName}"
                )
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()
                val mService = Calendar.Builder(transport, jsonFactory, mCredential)
                    .setApplicationName("MyFirstAndroidApp")
                    .build()
                val dateString = task.dateTime

                val utc = TimeZone.getTimeZone("UTC")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateFormat.timeZone = utc

                val taskDateJava: java.util.Date = dateFormat.parse(dateString.substring(0, 10))!!
                val startDate = DateTime(true, taskDateJava.time, 0)

                val calendar = java.util.Calendar.getInstance(utc)
                calendar.time = taskDateJava
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                val endDate = DateTime(true, calendar.time.time, 0)

                val event = Event()
                    .setSummary(task.name)
                    .setDescription(task.description)
                event.start = EventDateTime().setDate(startDate)
                event.end = EventDateTime().setDate(endDate)

                Log.i("TaskListAdapter", "Event Name: ${event.summary}\nEvent Desc: ${event.description}\nEvent Date: ${event.start} ==> ${event.end}")

                mService.events().insert("primary",event).execute()
                viewModel.deleteTask(task.id)
                withContext(Dispatchers.Main) {
                    OneActionDialog(context).show(
                        drawable = R.drawable.ic_dialog_success,
                        title = "Add to calendar Success!",
                        message = "",
                        onConfirmClickListener = {
                            viewModel.onEvent(ViewTaskEvent.OnUserSelectedDate(viewModel.state.value.selectedDate))
                            viewModel.onEvent(ViewTaskEvent.OnUserSelectedMonth(viewModel.state.value.selectedMonthPosition,viewModel.state.value.selectedMonth))
                        }
                    )
                }


            } catch (e: UserRecoverableAuthIOException){
                withContext(Dispatchers.Main) {
                    onPermissionNeeded(e.intent)
                }
            }
            catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("TaskListAdapter",e.message.toString())
                    OneActionDialog(context).show(
                        drawable = R.drawable.ic_dialog_no,
                        title = "Error",
                        message = e.message.toString(),
                        onConfirmClickListener = {
                        }
                    )
                }

            }
        }
    }

}