package com.b_lam.resplash.ui.user

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.util.loadPhotoUrl
import com.b_lam.resplash.util.loadProfilePicture
import kotlinx.android.synthetic.main.item_user_default.view.*

class UserViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        user: User?,
        callback: UserAdapter.ItemEventCallback
    ) {
        user?.let {
            with(itemView) {
                user_image_view.loadProfilePicture(user)
                full_name_text_view.text = user.name ?: context.getString(R.string.unknown)
                username_text_view.text =
                    context.getString(R.string.username_template,
                        user.username ?: context.getString(R.string.unknown))
                setOnClickListener { callback.onUserClick(user) }
                user.photos?.let { photos ->
                    val imageViews =
                        listOf(photo_one_image_view, photo_two_image_view, photo_three_image_view)
                    val cardViews =
                        listOf(photo_one_card_view, photo_two_card_view, photo_three_card_view)
                    if (photos.isNotEmpty()) {
                        imageViews.forEachIndexed { index, imageView ->
                            photos.getOrNull(index)?.let { photo ->
                                imageView.loadPhotoUrl(
                                    photo.urls.small,
                                    ContextCompat.getColor(context, R.color.grey_300)
                                )
                                imageView.setOnClickListener { callback.onPhotoClick(photo) }
                            }
                        }
                    }
                    cardViews.forEach { it.isVisible = photos.isNotEmpty() }
                }
            }
        }
    }
}