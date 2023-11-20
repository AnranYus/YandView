package com.lsp.view.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lsp.view.R

class TagsAdapter(private val tagList:List<String>):RecyclerView.Adapter<TagsAdapter.ViewHolder>() {
    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val tagItem = view.findViewById<TextView>(R.id.tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.tags_item_layout, parent, false)
        val viewHolder = ViewHolder(view)

        return viewHolder
    }

    override fun getItemCount() = tagList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tagItem.text = tagList[position]
    }
}