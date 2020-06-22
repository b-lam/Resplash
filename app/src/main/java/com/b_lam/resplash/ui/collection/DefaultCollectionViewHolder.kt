package com.b_lam.resplash.ui.collection

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.util.getPhotoUrl
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail
import com.b_lam.resplash.util.loadProfilePicture
import com.b_lam.resplash.util.margin
import kotlinx.android.synthetic.main.item_collection_default.view.*

class DefaultCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        collection: Collection?,
        loadQuality: String?,
        showUser: Boolean,
        callback: CollectionAdapter.ItemEventCallback
    ) {
        collection?.let {
            with(itemView) {
                if (showUser) {
                    itemView.margin(bottom = resources.getDimensionPixelSize(R.dimen.keyline_6))
                    collection.user?.let { user ->
                        user_container.isVisible = true
                        user_container.setOnClickListener { callback.onUserClick(user) }
                        user_image_view.loadProfilePicture(user)
                        user_text_view.text = user.name ?: context.getString(R.string.unknown)
                    }
                }
                collection.cover_photo?.let { photo ->
                    val url = getPhotoUrl(photo, loadQuality)
                    collection_image_view.minimumHeight = resources.getDimensionPixelSize(R.dimen.collection_max_height)
                    collection_image_view.loadPhotoUrlWithThumbnail(url, photo.urls.thumb, photo.color, true)
                }
                collection_name_text_view.text = collection.title
                collection_count_text_view.text = resources.getQuantityString(
                    R.plurals.photos,
                    collection.total_photos,
                    collection.total_photos
                )
                collection_private_icon.isVisible = collection.private ?: false
                setOnClickListener { callback.onCollectionClick(collection) }
            }
        }
    }
}
