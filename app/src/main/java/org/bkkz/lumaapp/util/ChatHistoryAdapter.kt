package org.bkkz.lumaapp.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R

class ChatHistoryAdapter(private val items: List<String>) : RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chat: TextView = view.findViewById(R.id.txtview_viewholder_chat_history)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_chat_timeline, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.chat.text = items[position]
    }

    override fun getItemCount() = items.size

}