package com.b_lam.resplash.ui.widget.recyclerview

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class RecyclerViewPaginator(
    private val onLoadMore: (() -> Unit)? = null,
    val isLoading: () -> Boolean,
    val onLastPage: () -> Boolean
) {

    fun attach(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager ?: return

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(list: RecyclerView, newState: Int) {
                super.onScrollStateChanged(list, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (onLastPage() || isLoading()) return

                    if (findLastVisibleItemPosition(layoutManager) + 1 == layoutManager.itemCount) {
                        onLoadMore?.invoke()
                    }
                }
            }

            override fun onScrolled(list: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(list, dx, dy)

                if (onLastPage() || isLoading()) return

                if (findLastVisibleItemPosition(layoutManager) + 1 == layoutManager.itemCount) {
                    onLoadMore?.invoke()
                }
            }
        })
    }

    private fun findLastVisibleItemPosition(layoutManager: RecyclerView.LayoutManager?): Int {
        return when (layoutManager) {
            is LinearLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is GridLayoutManager -> layoutManager.findLastVisibleItemPosition()
            is StaggeredGridLayoutManager -> layoutManager.findLastVisibleItemPositions(null).first()
            else -> RecyclerView.NO_POSITION
        }
    }
}