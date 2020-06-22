package com.b_lam.resplash.ui.photo

import android.animation.Animator
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.util.getPhotoUrl
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail
import com.b_lam.resplash.util.setAspectRatio
import kotlinx.android.synthetic.main.item_photo_grid.view.*

class GridPhotoViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

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