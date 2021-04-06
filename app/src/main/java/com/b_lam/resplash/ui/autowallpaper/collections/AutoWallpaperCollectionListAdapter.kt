package com.b_lam.resplash.ui.autowallpaper.collections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection

class AutoWallpaperCollectionListAdapter(
    private val itemType: ItemType,
    private val callback: ItemEventCallback
) : ListAdapter<AutoWallpaperCollection, RecyclerView.ViewHolder>(diffCallback) {

    private var selectedCollectionIds = listOf<Int>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_auto_wallpaper_selected_collection ->
                SelectedAutoWallpaperCollectionViewHolder(view)
            R.layout.item_auto_wallpaper_featured_collection ->
                FeaturedAutoWallpaperCollectionViewHolder(view)
            R.layout.item_auto_wallpaper_popular_collection ->
                PopularAutoWallpaperCollectionViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(getItem(position)) {
            when (getItemViewType(position)) {
                R.layout.item_auto_wallpaper_selected_collection ->
                    (holder as SelectedAutoWallpaperCollectionViewHolder)
                        .bind(this, callback)
                R.layout.item_auto_wallpaper_featured_collection ->
                    (holder as FeaturedAutoWallpaperCollectionViewHolder)
                        .bind(this, selectedCollectionIds, callback)
                R.layout.item_auto_wallpaper_popular_collection ->
                    (holder as PopularAutoWallpaperCollectionViewHolder)
                        .bind(this, selectedCollectionIds, callback)
            }
        }
    }

    override fun getItemViewType(position: Int) = when (itemType) {
        ItemType.SELECTED -> R.layout.item_auto_wallpaper_selected_collection
        ItemType.FEATURED -> R.layout.item_auto_wallpaper_featured_collection
        ItemType.POPULAR -> R.layout.item_auto_wallpaper_popular_collection
    }

    fun setSelectedCollectionIds(selectedCollectionIds: List<Int>) {
        this.selectedCollectionIds = selectedCollectionIds
        notifyDataSetChanged()
    }

    enum class ItemType {
        SELECTED,
        FEATURED,
        POPULAR
    }

    interface ItemEventCallback {

        fun onCollectionClick(id: Int)
        fun onAddClick(collection: AutoWallpaperCollection)
        fun onRemoveClick(id: Int)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<AutoWallpaperCollection>() {
            override fun areItemsTheSame(oldItem: AutoWallpaperCollection, newItem: AutoWallpaperCollection) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: AutoWallpaperCollection, newItem: AutoWallpaperCollection) = oldItem == newItem
        }
    }
}