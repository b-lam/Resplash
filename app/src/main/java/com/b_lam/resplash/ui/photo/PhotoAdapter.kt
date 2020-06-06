package com.b_lam.resplash.ui.photo

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.PreloadPagedListAdapter
import com.b_lam.resplash.util.*
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class PhotoAdapter(
    private val context: Context?,
    private val callback: ItemEventCallback,
    private val showUser: Boolean,
    sharedPreferencesRepository: SharedPreferencesRepository
) : PreloadPagedListAdapter<Photo>(diffCallback) {

    private val layout = sharedPreferencesRepository.layout
    private val loadQuality = sharedPreferencesRepository.loadQuality

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.item_photo_default -> DefaultPhotoViewHolder(view)
            R.layout.item_photo_minimal -> MinimalPhotoViewHolder(view)
            R.layout.item_photo_grid -> GridPhotoViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(getItem(position)) {
            when (getItemViewType(position)) {
                R.layout.item_photo_default ->
                    (holder as DefaultPhotoViewHolder).bind(this, loadQuality, showUser, callback)
                R.layout.item_photo_minimal ->
                    (holder as MinimalPhotoViewHolder).bind(this, loadQuality, callback)
                R.layout.item_photo_grid ->
                    (holder as GridPhotoViewHolder).bind(this, loadQuality, callback)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            layout == LAYOUT_DEFAULT && orientation == Configuration.ORIENTATION_PORTRAIT ->
                R.layout.item_photo_default
            layout == LAYOUT_MINIMAL && orientation == Configuration.ORIENTATION_PORTRAIT ->
                R.layout.item_photo_minimal
            layout == LAYOUT_GRID || orientation == Configuration.ORIENTATION_LANDSCAPE ->
                R.layout.item_photo_grid
            else ->
                R.layout.item_photo_default
        }
    }

    override fun getPreloadRequestBuilder(item: Photo): RequestBuilder<*>? {
        context?.let {
            val url = getPhotoUrl(item, loadQuality)
            return GlideApp.with(it)
                .load(url)
                .thumbnail(GlideApp.with(it).load(item.urls.thumb))
                .transition(DrawableTransitionOptions.withCrossFade(CROSS_FADE_DURATION))
        }
        return null
    }

    interface ItemEventCallback {

        fun onPhotoClick(photo: Photo)
        fun onUserClick(user: User)
        fun onDownloadClick(photo: Photo)
        fun onLikeClick(photo: Photo, position: Int)
        fun onCollectClick(photo: Photo)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo) = oldItem == newItem
        }
    }
}
