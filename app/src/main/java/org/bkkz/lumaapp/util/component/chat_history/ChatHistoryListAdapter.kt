package org.bkkz.lumaapp.util.component.chat_history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R

class ChatHistoryListAdapter(
    private val isFromHome : Boolean,
    private val items: List<String>,
    private val listener: OnChatHistoryListener) : RecyclerView.Adapter<ChatHistoryListAdapter.ViewHolder>() {

    interface OnChatHistoryListener {
        fun onReadAllClicked(fullText: String)
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chat: TextView = view.findViewById(R.id.txtview_viewholder_chat_history)
        val readAllBtn : ConstraintLayout = view.findViewById(R.id.constraintlayout_viewholder_chat_history_readall)
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
        if(!isFromHome){
            holder.chat.setTextColor(holder.itemView.context.getColor(R.color.black))
        }
        if(items[position].length > 125){
            holder.chat.text = "${items[position].substring(0,125)}..."
            holder.readAllBtn.visibility = View.VISIBLE
            holder.readAllBtn.setOnClickListener {
                listener.onReadAllClicked(items[position])
            }
        }else{
            holder.chat.text = items[position]
            holder.readAllBtn.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size

}