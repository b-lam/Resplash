package com.b_lam.resplash.ui.collection

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.BasePagedListAdapter
import com.b_lam.resplash.util.LAYOUT_DEFAULT
import com.b_lam.resplash.util.LAYOUT_GRID
import com.b_lam.resplash.util.LAYOUT_MINIMAL

class CollectionAdapter(
    private val callback: ItemEventCallback,
    private val showUser: Boolean,
    sharedPreferencesRepository: SharedPreferencesRepository
) : BasePagedListAdapter<Collection>(diffCallback) {

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

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val collectionImageView: ImageView? = holder.itemView.findViewById(R.id.collection_image_view)
        val userImageView: ImageView? = holder.itemView.findViewById(R.id.user_image_view)
        collectionImageView?.let { GlideApp.with(it.context).clear(it) }
        userImageView?.let { GlideApp.with(it.context).clear(it) }
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