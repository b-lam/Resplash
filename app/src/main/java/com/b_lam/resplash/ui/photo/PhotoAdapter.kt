package com.b_lam.resplash.ui.photo

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.BasePagedListAdapter
import com.b_lam.resplash.util.LAYOUT_DEFAULT
import com.b_lam.resplash.util.LAYOUT_GRID
import com.b_lam.resplash.util.LAYOUT_MINIMAL

class PhotoAdapter(
    private val callback: ItemEventCallback,
    private val showUser: Boolean,
    sharedPreferencesRepository: SharedPreferencesRepository
) : BasePagedListAdapter<Photo>(diffCallback) {

    private val layout = sharedPreferencesRepository.layout
    private val loadQuality = sharedPreferencesRepository.loadQuality
    private val longPressDownload = sharedPreferencesRepository.longPressDownload

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
                    (holder as DefaultPhotoViewHolder).bind(this, loadQuality, showUser, longPressDownload, callback)
                R.layout.item_photo_minimal ->
                    (holder as MinimalPhotoViewHolder).bind(this, loadQuality, longPressDownload, callback)
                R.layout.item_photo_grid ->
                    (holder as GridPhotoViewHolder).bind(this, loadQuality, longPressDownload, callback)
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

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val photoImageView: ImageView? = holder.itemView.findViewById(R.id.photo_image_view)
        val userImageView: ImageView? = holder.itemView.findViewById(R.id.user_image_view)
        photoImageView?.let { GlideApp.with(it.context).clear(it) }
        userImageView?.let { GlideApp.with(it.context).clear(it) }
    }

    interface ItemEventCallback {

        fun onPhotoClick(photo: Photo)
        fun onUserClick(user: User)
        fun onLongClick(photo: Photo)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Photo, newItem: Photo) = oldItem == newItem
        }
    }
}
