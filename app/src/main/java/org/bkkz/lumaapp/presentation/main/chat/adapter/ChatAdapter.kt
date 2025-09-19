package org.bkkz.lumaapp.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.util.component.chat.ChatItem

class ChatAdapter(
    private val items: List<ChatItem>,
    private val onConfirmClick: (dbId: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_MODEL = 1
        private const val VIEW_TYPE_VIEW_TASK = 2
        private const val VIEW_TYPE_ADD_TASK = 3
        private const val VIEW_TYPE_EDIT_TASK = 4
        private const val VIEW_TYPE_DELETE_TASK = 5
        private const val VIEW_TYPE_WEB = 6
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.txtview_chat_user)

        fun bind(chat: ChatItem.ChatUser) {
            message.text = chat.message
        }
    }
    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.txtview_chat_model)

        fun bind(chat: ChatItem.ChatResponse) {
            message.text = chat.message
        }
    }
    inner class ViewTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName : TextView = itemView.findViewById(R.id.txtview_chat_view_task_name)
        val taskDesc : TextView = itemView.findViewById(R.id.txtview_chat_view_task_desc)
        val taskDate : TextView = itemView.findViewById(R.id.txtview_chat_view_task_date)

        fun bind(task: ChatItem.ChatGetTask) {
            taskName.text = task.taskName
            taskDesc.text = task.taskDesc
            taskDate.text = task.taskDateTime
        }
    }
    inner class AddTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName : TextView = itemView.findViewById(R.id.txtview_chat_add_task_name)
        val taskDesc : TextView = itemView.findViewById(R.id.txtview_chat_add_task_desc)
        val taskDate : TextView = itemView.findViewById(R.id.txtview_chat_add_task_date)
        val confirmBtn : TextView = itemView.findViewById(R.id.txtview_chat_add_task_btn)
        fun bind(task: ChatItem.ChatAddTask) {
            taskName.text = task.taskName
            taskDesc.text = task.taskDesc
            taskDate.text = task.taskDateTime
            confirmBtn.setOnClickListener {
                //TODO: Implement add service, then change color
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                onConfirmClick(task.roomDbId)
            }
        }
    }
    inner class EditTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName : TextView = itemView.findViewById(R.id.txtview_chat_edit_task_name)
        val taskDesc : TextView = itemView.findViewById(R.id.txtview_chat_edit_task_desc)
        val taskDate : TextView = itemView.findViewById(R.id.txtview_chat_edit_task_date)
        val confirmBtn : TextView = itemView.findViewById(R.id.txtview_chat_edit_task_btn)
        fun bind(task: ChatItem.ChatEditTask) {
            taskName.text = task.taskName
            taskDesc.text = task.taskDesc
            taskDate.text = task.taskDateTime
            confirmBtn.setOnClickListener {
                //TODO: Implement edit service, then change color
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                onConfirmClick(task.roomDbId)
            }
        }
    }
    inner class DeleteTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName : TextView = itemView.findViewById(R.id.txtview_chat_delete_task_name)
        val taskDesc : TextView = itemView.findViewById(R.id.txtview_chat_delete_task_desc)
        val taskDate : TextView = itemView.findViewById(R.id.txtview_chat_delete_task_date)
        val confirmBtn : TextView = itemView.findViewById(R.id.txtview_chat_delete_task_btn)
        fun bind(task: ChatItem.ChatDeleteTask) {
            taskName.text = task.taskName
            taskDesc.text = task.taskDesc
            taskDate.text = task.taskDateTime
            confirmBtn.setOnClickListener {
                //TODO: Implement delete service, then change color
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                onConfirmClick(task.roomDbId)
            }
        }
    }

    inner class WebViewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val webView : WebView = itemView.findViewById(R.id.webview_chat_web)

        fun bind(web: ChatItem.ChatWebSearch){
            webView.loadUrl(web.url)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ChatItem.ChatUser -> VIEW_TYPE_USER
            is ChatItem.ChatResponse -> VIEW_TYPE_MODEL
            is ChatItem.ChatGetTask -> VIEW_TYPE_VIEW_TASK
            is ChatItem.ChatAddTask -> VIEW_TYPE_ADD_TASK
            is ChatItem.ChatEditTask -> VIEW_TYPE_EDIT_TASK
            is ChatItem.ChatDeleteTask -> VIEW_TYPE_DELETE_TASK
            is ChatItem.ChatWebSearch -> VIEW_TYPE_WEB
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when(viewType){
            VIEW_TYPE_USER -> {
                val view = inflater.inflate(R.layout.chat_user, parent, false)
                UserViewHolder(view)
            }
            VIEW_TYPE_MODEL -> {
                val view = inflater.inflate(R.layout.chat_model, parent, false)
                ModelViewHolder(view)
            }
            VIEW_TYPE_VIEW_TASK -> {
                val view = inflater.inflate(R.layout.chat_view_task, parent, false)
                ViewTaskViewHolder(view)
            }
            VIEW_TYPE_ADD_TASK -> {
                val view = inflater.inflate(R.layout.chat_add_task, parent, false)
                AddTaskViewHolder(view)
            }
            VIEW_TYPE_EDIT_TASK -> {
                val view = inflater.inflate(R.layout.chat_edit_task, parent, false)
                EditTaskViewHolder(view)
            }
            VIEW_TYPE_DELETE_TASK -> {
                val view = inflater.inflate(R.layout.chat_delete_task, parent, false)
                DeleteTaskViewHolder(view)
            }
            VIEW_TYPE_WEB -> {
                val view = inflater.inflate(R.layout.chat_webview, parent, false)
                WebViewViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when(holder){
            is UserViewHolder -> holder.bind(items[position] as ChatItem.ChatUser)
            is ModelViewHolder -> holder.bind(items[position] as ChatItem.ChatResponse)
            is ViewTaskViewHolder -> holder.bind(items[position] as ChatItem.ChatGetTask)
            is AddTaskViewHolder -> holder.bind(items[position] as ChatItem.ChatAddTask)
            is EditTaskViewHolder -> holder.bind(items[position] as ChatItem.ChatEditTask)
            is DeleteTaskViewHolder -> holder.bind(items[position] as ChatItem.ChatDeleteTask)
            is WebViewViewHolder -> holder.bind(items[position] as ChatItem.ChatWebSearch)
        }
    }

    override fun getItemCount(): Int = items.size

}