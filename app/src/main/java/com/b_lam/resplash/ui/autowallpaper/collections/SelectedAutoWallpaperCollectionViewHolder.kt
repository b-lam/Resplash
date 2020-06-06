package com.b_lam.resplash.ui.autowallpaper.collections

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.util.loadPhotoUrl
import kotlinx.android.synthetic.main.item_auto_wallpaper_selected_collection.view.*

class SelectedAutoWallpaperCollectionViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        collection: AutoWallpaperCollection?,
        callback: AutoWallpaperCollectionListAdapter.ItemEventCallback
    ) {
        collection?.let {
            with(itemView) {
                collection_name_text_view.text = collection.title
                collection.cover_photo?.let { collection_image_view.loadPhotoUrl(it) }
                collection_card_view.setOnClickListener { collection.id?.let { id -> callback.onCollectionClick(id) } }
                remove_button.setOnClickListener { collection.id?.let { id -> callback.onRemoveClick(id) } }
            }
        }
    }
}