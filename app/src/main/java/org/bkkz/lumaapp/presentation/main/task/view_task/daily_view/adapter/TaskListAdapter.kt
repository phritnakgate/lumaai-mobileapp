package org.bkkz.lumaapp.presentation.main.task.view_task.daily_view.adapter

import android.accounts.Account
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
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.enums.TaskCategory
import org.bkkz.lumaapp.util.enums.TaskPriority
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class TaskListAdapter(
    private val onPermissionNeeded: (Intent) -> Unit,
    private val viewModel : ViewTaskViewModel) :
    ListAdapter<Task, TaskListAdapter.ViewHolder>(TaskListDiffCallback()) {

    interface OnTaskCheckedListener {
        fun onTaskChecked(item: Task)
    }

    private var onTaskCheckedListener: OnTaskCheckedListener? = null
    fun setOnTaskCheckedListener(listener: OnTaskCheckedListener) {
        this.onTaskCheckedListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val taskHead: ConstraintLayout = view.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = view.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = view.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = view.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = view.findViewById(R.id.txtview_recycler_task_desc)
        val taskCategory: TextView = view.findViewById(R.id.txtview_recycler_task_category)
        val taskPriority: TextView = view.findViewById(R.id.txtview_recycler_task_priority)
        val taskEdit: ImageView = view.findViewById(R.id.imgview_recycler_task_edit)
        val ggCalendar: ImageView = view.findViewById(R.id.imgview_recycler_task_ggcalendar)
        val ggCalendarText : TextView = view.findViewById(R.id.txtview_recycler_task_ggcalendar)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val sharedPref = holder.itemView.context.getSharedPreferences("userSession", MODE_PRIVATE)
        val userEmail = sharedPref.getString("googleCalendarEmail", null)

        val taskTime: String = getItem(position).dateTime
        var isFinished: Boolean = getItem(position).isFinished

        if (isFinished) {
            holder.taskHead.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.rect_disabled_color)
            holder.taskCheck.setImageResource(R.drawable.ic_task_success)
        }else {
            holder.taskHead.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.rect_secondary)
            holder.taskCheck.setImageResource(R.drawable.circ_white)
        }
        holder.taskName.text = getItem(position).name
        holder.taskTime.text =
            OffsetDateTime.parse(taskTime).format(DateTimeFormatter.ofPattern("HH:mm"))
        if(getItem(position).description.isEmpty()){
            holder.taskDesc.text = holder.itemView.context.getString(R.string.view_task_no_description)
            holder.taskDesc.setTextColor(holder.itemView.context.getColor(R.color.disabled))
        }else{
            holder.taskDesc.text = getItem(position).description
            holder.taskDesc.setTextColor(holder.itemView.context.getColor(R.color.black))
        }
        holder.taskEdit.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditTaskActivity::class.java)
            intent.putExtra("TASK_DATA", getItem(position))
            context.startActivity(intent)
        }
        holder.taskCheck.setOnClickListener {
            isFinished = !isFinished
            onTaskCheckedListener?.onTaskChecked(getItem(position))
            if (isFinished) {
                holder.taskHead.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.rect_disabled_color
                )
                holder.taskCheck.setImageResource(R.drawable.ic_task_success)
            } else {
                holder.taskHead.background =
                    ContextCompat.getDrawable(holder.itemView.context, R.drawable.rect_secondary)
                holder.taskCheck.setImageResource(R.drawable.circ_white)
            }
        }
        holder.ggCalendar.setOnClickListener {
            if(userEmail == null){
                OneActionDialog(holder.itemView.context).show(
                    drawable = R.drawable.ic_dialog_no,
                    title = "Error",
                    message = "Please connect to Google Calendar first!",
                    onConfirmClickListener = {}
                )
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val mCredential = GoogleAccountCredential.usingOAuth2(
                        holder.itemView.context,
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
                    val dateString = getItem(position).dateTime

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
                        .setSummary(getItem(position).name)
                        .setDescription(getItem(position).description)
                    event.start = EventDateTime().setDate(startDate)
                    event.end = EventDateTime().setDate(endDate)

                    Log.i("TaskListAdapter", "Event Name: ${event.summary}\nEvent Desc: ${event.description}\nEvent Date: ${event.start} ==> ${event.end}")

                    mService.events().insert("primary",event).execute()
                    viewModel.deleteTask(getItem(position).id)
                    withContext(Dispatchers.Main) {
                        OneActionDialog(holder.itemView.context).show(
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
                        OneActionDialog(holder.itemView.context).show(
                            drawable = R.drawable.ic_dialog_no,
                            title = "Error",
                            message = e.message.toString(),
                            onConfirmClickListener = {}
                        )
                    }

                }
            }

        }
        val category = getItem(position).category
        val categoryColorRes = when (category) {
            0 -> R.color.category_color_0
            1 -> R.color.category_color_1
            2 -> R.color.category_color_2
            3 -> R.color.category_color_3
            4 -> R.color.category_color_4
            else -> R.color.category_color_0
        }
        holder.taskCategory.text = TaskCategory.fromInt(category)?.displayName
        holder.taskCategory.backgroundTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            categoryColorRes
        )
        val priority = getItem(position).priority
        val priorityColorRes = when (priority) {
            0 -> R.color.danger
            1 -> R.color.secondary
            2 -> R.color.primary
            else -> R.color.primary
        }
        holder.taskPriority.text = TaskPriority.fromInt(priority)?.displayName
        holder.taskPriority.backgroundTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            priorityColorRes
        )
        if(getItem(position).isGoogleCalendarTask){
            holder.ggCalendarText.visibility = View.GONE
        }else{
            holder.ggCalendarText.visibility = View.VISIBLE
        }
    }
    
    class TaskListDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

}