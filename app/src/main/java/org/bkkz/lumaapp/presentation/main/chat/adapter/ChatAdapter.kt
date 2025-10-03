package org.bkkz.lumaapp.presentation.main.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.PdfViewerActivity.Companion.ENABLE_FILE_DOWNLOAD
import com.rajat.pdfviewer.util.saveTo
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.util.component.chat.ChatItem
import org.bkkz.lumaapp.util.enums.LocalChatFlag
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ChatAdapter(
    private val items: List<ChatItem>,
    private val onConfirmClick: (dbId: Int, flag: Int, task: Task) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_MODEL = 1
        private const val VIEW_TYPE_VIEW_TASK = 2
        private const val VIEW_TYPE_ADD_TASK = 3
        private const val VIEW_TYPE_EDIT_TASK = 4
        private const val VIEW_TYPE_DELETE_TASK = 5
        private const val VIEW_TYPE_WEB = 6
        private const val VIEW_TYPE_GENFORM = 7
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

            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm")
            val formattedString = OffsetDateTime.parse(task.taskDateTime).format(outputFormatter)
            taskDate.text = formattedString
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

            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm")
            val formattedString = OffsetDateTime.parse(task.taskDateTime).format(outputFormatter)
            taskDate.text = formattedString

            if(task.actionCompleted){
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                confirmBtn.isEnabled = false
            } else {
                confirmBtn.setBackgroundResource(R.drawable.rect_danger_btn)
                confirmBtn.isEnabled = true
            }

            confirmBtn.setOnClickListener {
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                confirmBtn.isEnabled = false

                onConfirmClick(task.roomDbId, LocalChatFlag.CHAT_ADD_TASK.flag ,Task(
                    id="",
                    name=task.taskName,
                    description=task.taskDesc,
                    dateTime=task.taskDateTime,
                    isFinished = false,
                    userId = ""
                ))
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

            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm")
            val formattedString = OffsetDateTime.parse(task.taskDateTime).format(outputFormatter)
            taskDate.text = formattedString

            if(task.actionCompleted){
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                confirmBtn.isEnabled = false
            } else {
                confirmBtn.setBackgroundResource(R.drawable.rect_danger_btn)
                confirmBtn.isEnabled = true
            }

            confirmBtn.setOnClickListener {
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                confirmBtn.isEnabled = false

                onConfirmClick(task.roomDbId, LocalChatFlag.CHAT_EDIT_TASK.flag ,Task(
                    id=task.taskId,
                    name=task.taskName,
                    description=task.taskDesc,
                    dateTime=task.taskDateTime,
                    isFinished = false,
                    userId = ""
                ))
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

            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm")
            val formattedString = OffsetDateTime.parse(task.taskDateTime).format(outputFormatter)
            taskDate.text = formattedString

            if(task.actionCompleted){
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                confirmBtn.isEnabled = false
            } else {
                confirmBtn.setBackgroundResource(R.drawable.rect_danger_btn)
                confirmBtn.isEnabled = true
            }

            confirmBtn.setOnClickListener {
                confirmBtn.setBackgroundResource(R.drawable.rect_disabled_color_btn)
                confirmBtn.isEnabled = false
                onConfirmClick(task.roomDbId, LocalChatFlag.CHAT_DELETE_TASK.flag ,Task(
                    id=task.taskId,
                    name=task.taskName,
                    description=task.taskDesc,
                    dateTime=task.taskDateTime,
                    isFinished = false,
                    userId = ""
                ))
            }
        }
    }

    inner class WebViewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val webView : WebView = itemView.findViewById(R.id.webview_chat_web)

        fun bind(web: ChatItem.ChatWebSearch){
            webView.loadUrl(web.url)
        }
    }

    inner class GenFormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val chatArea : ConstraintLayout = itemView.findViewById(R.id.constraintlayout_chat_genform)
        val txtFileName : TextView = itemView.findViewById(R.id.txtview_chat_genform)

        fun bind(genForm: ChatItem.ChatGenForm){
            chatArea.setOnClickListener {
                val pdfViewerActivity = PdfViewerActivity.launchPdfFromPath(
                    context = itemView.context,
                    path = genForm.url,
                    pdfTitle = File(genForm.url).name,
                    saveTo = saveTo.ASK_EVERYTIME,
                    fromAssets = false
                )
                pdfViewerActivity.putExtra(ENABLE_FILE_DOWNLOAD, true)
                itemView.context.startActivity(pdfViewerActivity)
            }
            txtFileName.text = File(genForm.url).name

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
            is ChatItem.ChatGenForm -> VIEW_TYPE_GENFORM
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
            VIEW_TYPE_GENFORM -> {
                val view = inflater.inflate(R.layout.chat_genform, parent, false)
                GenFormViewHolder(view)
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
            is GenFormViewHolder -> holder.bind(items[position] as ChatItem.ChatGenForm)
        }
    }

    override fun getItemCount(): Int = items.size

}