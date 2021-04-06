package com.b_lam.resplash.ui.photo

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.databinding.ItemPhotoMinimalBinding
import com.b_lam.resplash.util.getPhotoUrl
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail
import com.b_lam.resplash.util.setAspectRatio

class MinimalPhotoViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemPhotoMinimalBinding by viewBinding()

    fun bind(
        photo: Photo?,
        loadQuality: String?,
        longPressDownload: Boolean,
        callback: PhotoAdapter.ItemEventCallback
    ) {
        with(binding) {
            photo?.let {
                val url = getPhotoUrl(photo, loadQuality)
                photoImageView.setAspectRatio(photo.width, photo.height)
                photoImageView.loadPhotoUrlWithThumbnail(url, photo.urls.thumb, photo.color)
                photoImageView.setOnClickListener { callback.onPhotoClick(photo) }
                if (longPressDownload) {
                    photoImageView.setOnLongClickListener {
                        callback.onLongClick(photo, checkAnimationView)
                        true
                    }
                }
            }
        }
    }
}