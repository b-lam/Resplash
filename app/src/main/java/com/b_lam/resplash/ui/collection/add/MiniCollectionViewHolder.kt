package com.b_lam.resplash.ui.collection.add

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail
import kotlinx.android.synthetic.main.item_collection_mini.view.*

class MiniCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        collection: Collection?,
        currentUserCollectionIds: List<Int>?,
        callback: AddCollectionAdapter.ItemEventCallback
    ) {
        collection?.let {
            with(itemView) {
                collection.cover_photo?.let { photo ->
                    collection_image_view.loadPhotoUrlWithThumbnail(
                        photo.urls.regular, photo.urls.thumb, photo.color, true)
                } ?: run {
                    GlideApp.with(context).clear(collection_image_view)
                    collection_image_view.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.transparent))
                }
                collection_name_text_view.text = collection.title
                photos_count_text_view.text = resources.getQuantityString(
                    R.plurals.photos,
                    collection.total_photos,
                    collection.total_photos
                )
                collection_private_icon.isVisible = collection.private == true
                val photoInCollection = currentUserCollectionIds?.contains(collection.id) == true
                collection_added_icon.isVisible = photoInCollection
                image_overlay.setBackgroundColor(ContextCompat.getColor(context,
                    if (photoInCollection) R.color.green_overlay else R.color.black_overlay))
                setOnClickListener {
                    callback.onCollectionClick(collection, this, absoluteAdapterPosition)
                }
            }
        }
    }
}