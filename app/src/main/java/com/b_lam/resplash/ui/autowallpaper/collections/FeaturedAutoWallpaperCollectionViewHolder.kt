package com.b_lam.resplash.ui.autowallpaper.collections

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.databinding.ItemAutoWallpaperFeaturedCollectionBinding
import com.b_lam.resplash.util.loadPhotoUrl

class FeaturedAutoWallpaperCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemAutoWallpaperFeaturedCollectionBinding by viewBinding()

    fun bind(
        collection: AutoWallpaperCollection?,
        selectedCollectionIds: List<String>,
        callback: AutoWallpaperCollectionListAdapter.ItemEventCallback
    ) {
        with(binding) {
            collection?.let {
                collectionNameTextView.text = collection.title
                userNameTextView.text = itemView.context.getString(R.string.curated_by_template, collection.user_name)
                collection.cover_photo?.let { collectionImageView.loadPhotoUrl(it) }
                collectionCardView.setOnClickListener {
                    callback.onCollectionClick(collection.id)
                }
                val isCollectionSelected = selectedCollectionIds.contains(collection.id)
                addButton.setImageResource(
                    if (isCollectionSelected) {
                        R.drawable.ic_remove_circle_outline_24dp
                    } else {
                        R.drawable.ic_add_circle_outline_24dp
                    }
                )
                addButton.setOnClickListener {
                    if (isCollectionSelected) {
                        callback.onRemoveClick(collection.id)
                    } else {
                        callback.onAddClick(collection)
                    }
                }
            }
        }
    }
}