package com.b_lam.resplash.ui.photo.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.data.photo.model.Tag
import com.b_lam.resplash.databinding.ItemPhotoTagBinding

class TagViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemPhotoTagBinding by viewBinding()

    fun bind(
        tag: Tag?,
        callback: TagAdapter.ItemEventCallback
    ) {
        with(binding) {
            tag?.title?.let { title ->
                tagChip.text = title
                itemView.setOnClickListener { callback.onTagClick(title) }
            }
        }
    }
}