package com.b_lam.resplash.ui.photo.detail

import android.text.SpannableStringBuilder
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_photo_exif.view.*

class ExifViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        titleRes: Int,
        value: SpannableStringBuilder
    ) {
        with(itemView) {
            exif_title_text_view.setText(titleRes)
            exif_value_text_view.text = value
        }
    }
}