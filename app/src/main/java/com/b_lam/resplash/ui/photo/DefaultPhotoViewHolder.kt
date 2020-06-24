package com.b_lam.resplash.ui.photo

import android.animation.Animator
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
        longPressDownload: Boolean,
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
                    sponsored_text_view.isVisible = photo.sponsorship != null
                }
                val url = getPhotoUrl(photo, loadQuality)
                photo_image_view.setAspectRatio(photo.width, photo.height)
                photo_image_view.loadPhotoUrlWithThumbnail(url, photo.urls.thumb, photo.color)
                photo_image_view.setOnClickListener { callback.onPhotoClick(photo) }
                if (longPressDownload) {
                    photo_image_view.setOnLongClickListener {
                        callback.onLongClick(photo)
                        check_animation_view.isVisible = true
                        check_animation_view.playAnimation()
                        check_animation_view.addAnimatorListener(object :
                            Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {}
                            override fun onAnimationCancel(animation: Animator?) {}
                            override fun onAnimationStart(animation: Animator?) {}
                            override fun onAnimationEnd(animation: Animator?) {
                                check_animation_view.removeAnimatorListener(this)
                                check_animation_view.isVisible = false
                            }
                        })
                        true
                    }
                }
            }
        }
    }
}
