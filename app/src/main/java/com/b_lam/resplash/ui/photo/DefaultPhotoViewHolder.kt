package com.b_lam.resplash.ui.photo

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.databinding.ItemPhotoDefaultBinding
import com.b_lam.resplash.util.*

class DefaultPhotoViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemPhotoDefaultBinding by viewBinding()

    fun bind(
        photo: Photo?,
        loadQuality: String?,
        showUser: Boolean,
        longPressDownload: Boolean,
        callback: PhotoAdapter.ItemEventCallback
    ) {
        with(binding) {
            photo?.let {
                if (showUser) {
                    itemView.margin(bottom = itemView.resources.getDimensionPixelSize(R.dimen.keyline_6))
                    photo.user?.let { user ->
                        userContainer.isVisible = true
                        userContainer.setOnClickListener { callback.onUserClick(user) }
                        userImageView.loadProfilePicture(user)
                        userTextView.text = user.name ?: itemView.context.getString(R.string.unknown)
                    }
                    sponsoredTextView.isVisible = photo.sponsorship != null
                }
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
