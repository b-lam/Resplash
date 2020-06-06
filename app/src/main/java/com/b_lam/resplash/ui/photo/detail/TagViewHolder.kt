package com.b_lam.resplash.ui.photo.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.data.photo.model.Tag
import kotlinx.android.synthetic.main.item_photo_tag.view.*

class TagViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        tag: Tag?,
        callback: TagAdapter.ItemEventCallback
    ) {
        tag?.title?.let { title ->
            with(itemView) {
                tag_chip.text = title
                setOnClickListener { callback.onTagClick(title) }
            }
        }
    }
}