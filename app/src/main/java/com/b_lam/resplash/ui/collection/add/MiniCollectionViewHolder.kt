package com.b_lam.resplash.ui.collection.add

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.databinding.ItemCollectionMiniBinding
import com.b_lam.resplash.util.loadPhotoUrlWithThumbnail

class MiniCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemCollectionMiniBinding by viewBinding()

    fun bind(
        collection: Collection?,
        currentUserCollectionIds: List<Int>?,
        callback: AddCollectionAdapter.ItemEventCallback
    ) {
        with(binding) {
            collection?.let {
                collection.cover_photo?.let { photo ->
                    collectionImageView.loadPhotoUrlWithThumbnail(
                        photo.urls.regular, photo.urls.thumb, photo.color, true)
                } ?: run {
                    GlideApp.with(itemView.context).clear(collectionImageView)
                    collectionImageView.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.transparent))
                }
                collectionNameTextView.text = collection.title
                photosCountTextView.text = itemView.resources.getQuantityString(
                    R.plurals.photos,
                    collection.total_photos,
                    collection.total_photos
                )
                collectionPrivateIcon.isVisible = collection.private == true
                val photoInCollection = currentUserCollectionIds?.contains(collection.id) == true
                collectionAddedIcon.isVisible = photoInCollection
                imageOverlay.setBackgroundColor(ContextCompat.getColor(itemView.context,
                    if (photoInCollection) R.color.green_overlay else R.color.black_overlay))
                itemView.setOnClickListener {
                    callback.onCollectionClick(collection, itemView, bindingAdapterPosition)
                }
            }
        }
    }
}