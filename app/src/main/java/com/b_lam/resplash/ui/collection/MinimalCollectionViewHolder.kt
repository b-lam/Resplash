package com.b_lam.resplash.ui.collection

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.util.getPhotoUrl
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail
import kotlinx.android.synthetic.main.item_collection_minimal.view.*

class MinimalCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        collection: Collection?,
        loadQuality: String?,
        callback: CollectionAdapter.ItemEventCallback
    ) {
        collection?.let {
            with(itemView) {
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
                setOnClickListener { callback.onCollectionClick(collection) }
            }
        }
    }
}