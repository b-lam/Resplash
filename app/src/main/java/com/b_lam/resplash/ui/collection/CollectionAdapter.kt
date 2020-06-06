package com.b_lam.resplash.ui.collection

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.PreloadPagedListAdapter
import com.b_lam.resplash.util.*
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class CollectionAdapter(
    private val context: Context?,
    private val callback: ItemEventCallback,
    private val showUser: Boolean,
    sharedPreferencesRepository: SharedPreferencesRepository
) : PreloadPagedListAdapter<Collection>(diffCallback) {

    private val layout = sharedPreferencesRepository.layout
    private val loadQuality = sharedPreferencesRepository.loadQuality

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_collection_default -> DefaultCollectionViewHolder(view)
            R.layout.item_collection_minimal -> MinimalCollectionViewHolder(view)
            R.layout.item_collection_grid -> GridCollectionViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(getItem(position)) {
            when (getItemViewType(position)) {
                R.layout.item_collection_default ->
                    (holder as DefaultCollectionViewHolder).bind(this, loadQuality, showUser, callback)
                R.layout.item_collection_minimal ->
                    (holder as MinimalCollectionViewHolder).bind(this, loadQuality, callback)
                R.layout.item_collection_grid ->
                    (holder as GridCollectionViewHolder).bind(this, loadQuality, callback)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            layout == LAYOUT_DEFAULT && orientation == Configuration.ORIENTATION_PORTRAIT ->
                R.layout.item_collection_default
            layout == LAYOUT_MINIMAL && orientation == Configuration.ORIENTATION_PORTRAIT ->
                R.layout.item_collection_minimal
            layout == LAYOUT_GRID || orientation == Configuration.ORIENTATION_LANDSCAPE ->
                R.layout.item_collection_grid
            else ->
                R.layout.item_collection_default
        }
    }

    override fun getPreloadRequestBuilder(item: Collection): RequestBuilder<*>? {
        context?.let {
            item.cover_photo?.let {
                val url = getPhotoUrl(item.cover_photo, loadQuality)
                return GlideApp.with(context)
                    .load(url)
                    .thumbnail(GlideApp.with(context).load(item.cover_photo.urls.thumb))
                    .transition(DrawableTransitionOptions.withCrossFade(CROSS_FADE_DURATION))
            }
        }
        return null
    }

    interface ItemEventCallback {

        fun onCollectionClick(collection: Collection)
        fun onUserClick(user: User)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Collection>() {
            override fun areItemsTheSame(oldItem: Collection, newItem: Collection) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Collection, newItem: Collection) = oldItem == newItem
        }
    }
}