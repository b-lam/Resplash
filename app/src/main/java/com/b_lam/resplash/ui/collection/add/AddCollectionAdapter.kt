package com.b_lam.resplash.ui.collection.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection

class AddCollectionAdapter(
    private val callback: ItemEventCallback
) : ListAdapter<Collection, MiniCollectionViewHolder>(diffCallback) {

    private var currentUserCollectionIds: List<Int>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniCollectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collection_mini, parent, false)
        return MiniCollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MiniCollectionViewHolder, position: Int) {
        holder.bind(getItem(position), currentUserCollectionIds, callback)
    }

    fun setCurrentUserCollectionIds(currentUserCollectionIds: List<Int>?) {
        this.currentUserCollectionIds = currentUserCollectionIds
    }

    interface ItemEventCallback {

        fun onCollectionClick(collection: Collection, itemView: View, position: Int)
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<Collection>() {
            override fun areItemsTheSame(oldItem: Collection, newItem: Collection) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Collection, newItem: Collection) = oldItem == newItem
        }
    }
}