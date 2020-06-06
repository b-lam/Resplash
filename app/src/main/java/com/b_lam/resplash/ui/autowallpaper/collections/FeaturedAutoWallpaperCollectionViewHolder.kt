package com.b_lam.resplash.ui.autowallpaper.collections

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.util.loadPhotoUrl
import kotlinx.android.synthetic.main.item_auto_wallpaper_featured_collection.view.*

class FeaturedAutoWallpaperCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        collection: AutoWallpaperCollection?,
        selectedCollectionIds: List<Int>,
        callback: AutoWallpaperCollectionListAdapter.ItemEventCallback
    ) {
        collection?.let {
            with(itemView) {
                collection_name_text_view.text = collection.title
                user_name_text_view.text = context.getString(R.string.curated_by_template, collection.user_name)
                collection.cover_photo?.let { collection_image_view.loadPhotoUrl(it) }
                collection_card_view.setOnClickListener { collection.id?.let { id -> callback.onCollectionClick(id) } }
                val isCollectionSelected = selectedCollectionIds.contains(collection.id)
                add_button.setImageResource(
                    if (isCollectionSelected) {
                        R.drawable.ic_remove_circle_outline_24dp
                    } else {
                        R.drawable.ic_add_circle_outline_24dp
                    }
                )
                add_button.setOnClickListener {
                    if (isCollectionSelected) {
                        collection.id?.let { id -> callback.onRemoveClick(id) }
                    } else {
                        callback.onAddClick(collection)
                    }
                }
            }
        }
    }
}