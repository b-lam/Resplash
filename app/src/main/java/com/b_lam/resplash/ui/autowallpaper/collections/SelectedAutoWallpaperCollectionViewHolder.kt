package com.b_lam.resplash.ui.autowallpaper.collections

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.databinding.ItemAutoWallpaperSelectedCollectionBinding
import com.b_lam.resplash.util.loadPhotoUrl

class SelectedAutoWallpaperCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemAutoWallpaperSelectedCollectionBinding by viewBinding()

    fun bind(
        collection: AutoWallpaperCollection?,
        callback: AutoWallpaperCollectionListAdapter.ItemEventCallback
    ) {
        with(binding) {
            collection?.let {
                collectionNameTextView.text = collection.title
                collection.cover_photo?.let { collectionImageView.loadPhotoUrl(it) }
                collectionCardView.setOnClickListener {
                    callback.onCollectionClick(collection.id)
                }
                removeButton.setOnClickListener {
                    callback.onRemoveClick(collection.id)
                }
            }
        }
    }
}