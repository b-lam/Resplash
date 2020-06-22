package com.b_lam.resplash.ui.photo.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Tag

class TagAdapter(val callback: ItemEventCallback) : ListAdapter<Tag, TagViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }

    interface ItemEventCallback {

        fun onTagClick(tag: String)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Tag>() {
            override fun areItemsTheSame(oldItem: Tag, newItem: Tag) = oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: Tag, newItem: Tag) = oldItem == newItem
        }
    }
}