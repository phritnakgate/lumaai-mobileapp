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
import androidx.recyclerview.widget.RecyclerView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.edit_task.EditTaskActivity
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class TaskListAdapter(private val items: List<Task>) : RecyclerView.Adapter<TaskListAdapter.ViewHolder>() {

    interface OnTaskCheckedListener{
        fun onTaskChecked(item: Task)
    }

    private var onTaskCheckedListener : OnTaskCheckedListener? = null
    fun setOnTaskCheckedListener(listener: OnTaskCheckedListener){
        this.onTaskCheckedListener = listener
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val taskHead: ConstraintLayout = view.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = view.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = view.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = view.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = view.findViewById(R.id.txtview_recycler_task_desc)
        val taskEdit : ImageView = view.findViewById(R.id.imgview_recycler_task_edit)
        val ggCalendar : ImageView = view.findViewById(R.id.imgview_recycler_task_ggcalendar)
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
        val userEmail = sharedPref.getString("email", null)

        val taskTime : String = items[position].dateTime
        var isFinished : Boolean = items[position].isFinished

        if(isFinished){
            holder.taskHead.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rect_disabled_color)
            holder.taskCheck.setImageResource(R.drawable.ic_task_success)
        }
        holder.taskName.text = items[position].name
        holder.taskTime.text = OffsetDateTime.parse(taskTime).format(DateTimeFormatter.ofPattern("HH:mm"))
        holder.taskDesc.text = items[position].description
        holder.taskEdit.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditTaskActivity::class.java)
            intent.putExtra("TASK_DATA", items[position])
            context.startActivity(intent)
        }
        holder.taskCheck.setOnClickListener {
            isFinished = !isFinished
            onTaskCheckedListener?.onTaskChecked(items[position])
            if(isFinished){
                holder.taskHead.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rect_disabled_color)
                holder.taskCheck.setImageResource(R.drawable.ic_task_success)
            }else{
                holder.taskHead.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.rect_secondary)
                holder.taskCheck.setImageResource(R.drawable.circ_white)
            }
        }
        holder.ggCalendar.setOnClickListener {
            try{
                val mCredential = GoogleAccountCredential.usingOAuth2(
                    holder.itemView.context,
                    arrayListOf(CalendarScopes.CALENDAR)
                ).setBackOff(ExponentialBackOff()).apply {
                    selectedAccount = userEmail?.let { Account(it, "com.google") }
                }
                Log.d("TaskListAdapter", "Calendar credential account: ${mCredential?.selectedAccountName}")
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()
                val mService = Calendar.Builder(transport, jsonFactory, mCredential)
                    .setApplicationName("MyFirstAndroidApp")
                    .build()
                val event = Event()
                    .setSummary(items[position].name)
                    .setDescription(items[position].description)

            }catch (e: Exception){
                OneActionDialog(holder.itemView.context).show(
                    drawable = R.drawable.ic_dialog_no,
                    title = "Error",
                    message = e.message.toString(),
                    onConfirmClickListener = {}
                )
            }
        }
    }

    override fun getItemCount() = items.size

}