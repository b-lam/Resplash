package com.b_lam.resplash.ui.photo

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.util.getPhotoUrl
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail
import com.b_lam.resplash.util.setAspectRatio
import kotlinx.android.synthetic.main.item_photo_minimal.view.*

class MinimalPhotoViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        photo: Photo?,
        loadQuality: String?,
        longPressDownload: Boolean,
        callback: PhotoAdapter.ItemEventCallback
    ) {
        photo?.let {
            with(itemView) {
                val url = getPhotoUrl(photo, loadQuality)
                photo_image_view.setAspectRatio(photo.width, photo.height)
                photo_image_view.loadPhotoUrlWithThumbnail(url, photo.urls.thumb, photo.color)
                photo_image_view.setOnClickListener { callback.onPhotoClick(photo) }
                if (longPressDownload) {
                    photo_image_view.setOnLongClickListener {
                        callback.onLongClick(photo, check_animation_view)
                        true
                    }
                }
            }
        }
    }
}