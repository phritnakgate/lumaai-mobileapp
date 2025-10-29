package org.bkkz.lumaapp.presentation.main.chat.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.util.component.chat.QuickPromptLov

class QuickPromptAdapter(private val items: List<QuickPromptLov>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((String) -> Unit)? = null

    inner class HeadlineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHeadline : TextView = view.findViewById(R.id.txtview_quick_prompt_headline)

        fun bind(item: QuickPromptLov.Headline) {
            txtHeadline.text = item.headline
        }
    }

    inner class ItemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItem : TextView = view.findViewById(R.id.txtview_quick_prompt_item)

        fun bind(item: QuickPromptLov.Items) {
            txtItem.text = item.displayString
            txtItem.setOnClickListener {
                onItemClick?.invoke(item.prompt)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is QuickPromptLov.Headline -> 0
            is QuickPromptLov.Items -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> {
                val view = View.inflate(parent.context, R.layout.popup_quick_prompt_headline, null)
                HeadlineViewHolder(view)
            }
            1 -> {
                val view = View.inflate(parent.context, R.layout.popup_quick_prompt_item, null)
                ItemsViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is HeadlineViewHolder -> {
                val item = items[position] as QuickPromptLov.Headline
                holder.bind(item)
            }
            is ItemsViewHolder -> {
                val item = items[position] as QuickPromptLov.Items
                holder.bind(item)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}