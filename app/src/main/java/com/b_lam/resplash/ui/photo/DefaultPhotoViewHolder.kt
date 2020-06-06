package com.b_lam.resplash.ui.photo

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.util.*
import kotlinx.android.synthetic.main.item_photo_default.view.*

class DefaultPhotoViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        photo: Photo?,
        loadQuality: String?,
        showUser: Boolean,
        callback: PhotoAdapter.ItemEventCallback
    ) {
        photo?.let {
            with(itemView) {
                if (showUser) {
                    itemView.margin(bottom = resources.getDimensionPixelSize(R.dimen.keyline_6))
                    photo.user?.let { user ->
                        user_container.isVisible = true
                        user_container.setOnClickListener { callback.onUserClick(user) }
                        user_image_view.loadProfilePicture(user)
                        user_text_view.text = user.name ?: context.getString(R.string.unknown)
                    }
                }
                val url = getPhotoUrl(photo, loadQuality)
                photo_image_view.setAspectRatio(photo.width, photo.height)
                photo_image_view.loadPhotoUrlWithThumbnail(url, photo.urls.thumb, photo.color)
                photo_image_view.setOnClickListener { callback.onPhotoClick(photo) }
                setPhotoLikedByUser(photo.liked_by_user)
                if (photo.current_user_collections?.size ?: 0 > 0) {
                    collect_button.setImageResource(R.drawable.ic_bookmark_filled_18dp)
                }
                download_button.setOnClickListener { callback.onDownloadClick(photo) }
                like_button.setOnClickListener {
                    photo.liked_by_user = photo.liked_by_user?.not()
                    setPhotoLikedByUser(!(photo.liked_by_user ?: false))
                    callback.onLikeClick(photo, adapterPosition)
                }
                collect_button.setOnClickListener { callback.onCollectClick(photo) }
            }
        }
    }

    private fun setPhotoLikedByUser(liked: Boolean?) {
        if (liked == true) {
            itemView.like_button.setImageResource(R.drawable.ic_favorite_filled_18dp)
        } else {
            itemView.like_button.setImageResource(R.drawable.ic_favorite_border_18dp)
        }
    }
}
