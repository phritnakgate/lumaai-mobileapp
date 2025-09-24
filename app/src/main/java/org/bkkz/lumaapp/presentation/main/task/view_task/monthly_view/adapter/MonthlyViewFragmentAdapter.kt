package org.bkkz.lumaapp.presentation.main.task.view_task.monthly_view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.edit_task.EditTaskActivity
import org.bkkz.lumaapp.util.component.monthly_task_recycler.TimelineItem
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class MonthlyViewFragmentAdapter(private val items: List<TimelineItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

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
        val taskEdit : ImageView = itemView.findViewById(R.id.imgview_recycler_task_edit)
        fun bind(header: TimelineItem.TaskHeader) {
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
        }
    }

    inner class TaskBodyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskHead: ConstraintLayout = itemView.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = itemView.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = itemView.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = itemView.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = itemView.findViewById(R.id.txtview_recycler_task_desc)
        val taskEdit : ImageView = itemView.findViewById(R.id.imgview_recycler_task_edit)
        fun bind(data: TimelineItem.TaskBody) {
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
        }
    }
    inner class TaskFooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val taskHead: ConstraintLayout = itemView.findViewById(R.id.constraintlayout_recycler_task_head)
        val taskCheck: ImageView = itemView.findViewById(R.id.imgview_recycler_task_check)
        val taskName: TextView = itemView.findViewById(R.id.txtview_recycler_task_name)
        val taskTime: TextView = itemView.findViewById(R.id.txtview_recycler_task_time)
        val taskDesc: TextView = itemView.findViewById(R.id.txtview_recycler_task_desc)
        val taskEdit : ImageView = itemView.findViewById(R.id.imgview_recycler_task_edit)
        fun bind(data: TimelineItem.TaskFooter) {
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


}