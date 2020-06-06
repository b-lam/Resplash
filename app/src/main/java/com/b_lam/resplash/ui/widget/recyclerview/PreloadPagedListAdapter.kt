package com.b_lam.resplash.ui.widget.recyclerview

import android.content.res.Configuration
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader

abstract class PreloadPagedListAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, RecyclerView.ViewHolder>(diffCallback),
    ListPreloader.PreloadModelProvider<T> {

    var orientation = Configuration.ORIENTATION_PORTRAIT

    override fun getPreloadItems(position: Int): MutableList<T> {
        return currentList?.toMutableList() ?: mutableListOf()
    }
}