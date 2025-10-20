package org.bkkz.lumaapp.presentation.main.chat_history.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.util.component.chat_history.ChatHistoryItem
import org.bkkz.lumaapp.util.component.chat_history.ChatHistoryListAdapter
import org.bkkz.lumaapp.util.component.chat_history.ChatHistoryListDecoration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChatHistoryAdapter(private val items: List<ChatHistoryItem>, private val listener: OnHistoryInteractionListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ChatHistoryListAdapter.OnChatHistoryListener{

    companion object {
        private const val VIEW_TYPE_DATE = 1
        private const val VIEW_TYPE_HISTORY_LIST = 2
    }

    interface OnHistoryInteractionListener {
        fun onShowBottomSheet(fullText: String)
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatDate: TextView = itemView.findViewById(R.id.txtview_viewholder_chat_history_date)

        fun bind(header: ChatHistoryItem.ChatHistoryDate) {
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            chatDate.text = LocalDate.parse(header.date).format(outputFormatter)
        }
    }

    inner class HistoryListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val chats: RecyclerView = itemView.findViewById(R.id.recyclerview_chat_history_holder)

        init {
            chats.layoutManager = LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
            chats.addItemDecoration(ChatHistoryListDecoration(itemView.context, false))
        }

        fun bind(body: ChatHistoryItem.ChatHistoryLists){
            chats.adapter = ChatHistoryListAdapter(false,body.histories, this@ChatHistoryAdapter)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ChatHistoryItem.ChatHistoryDate -> VIEW_TYPE_DATE
            is ChatHistoryItem.ChatHistoryLists -> VIEW_TYPE_HISTORY_LIST
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType){
            VIEW_TYPE_DATE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_history_date, parent, false)
                DateViewHolder(view)
            }
            VIEW_TYPE_HISTORY_LIST -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chat_history_lists, parent, false)
                HistoryListViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when(holder) {
            is DateViewHolder -> {
                holder.bind(items[position] as ChatHistoryItem.ChatHistoryDate)
            }
            is HistoryListViewHolder -> {
                holder.bind(items[position] as ChatHistoryItem.ChatHistoryLists)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onReadAllClicked(fullText: String) {
        listener.onShowBottomSheet(fullText)
    }
}