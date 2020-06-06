package com.b_lam.resplash.ui.widget.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.RequestManager

class RecyclerViewPreloader<T>(
    requestManager: RequestManager,
    preloadModelProvider: PreloadModelProvider<T>,
    preloadDimensionProvider: PreloadSizeProvider<T>,
    maxPreload: Int
) : RecyclerView.OnScrollListener() {

    private val recyclerScrollListener: RecyclerToListViewScrollListener

    init {
        val listPreloader = ListPreloader(requestManager, preloadModelProvider, preloadDimensionProvider, maxPreload)
        recyclerScrollListener = RecyclerToListViewScrollListener(listPreloader)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        recyclerScrollListener.onScrolled(recyclerView, dx, dy)
    }
}