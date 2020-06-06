package com.b_lam.resplash.ui.widget.recyclerview

import android.widget.AbsListView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlin.math.abs

class RecyclerToListViewScrollListener(
    private val scrollListener: AbsListView.OnScrollListener
) : RecyclerView.OnScrollListener() {

    private var lastFirstVisible = -1
    private var lastVisibleCount = -1
    private var lastItemCount = -1

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

        val listViewState: Int = when (newState) {
            RecyclerView.SCROLL_STATE_DRAGGING -> AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
            RecyclerView.SCROLL_STATE_IDLE -> AbsListView.OnScrollListener.SCROLL_STATE_IDLE
            RecyclerView.SCROLL_STATE_SETTLING -> AbsListView.OnScrollListener.SCROLL_STATE_FLING
            else -> UNKNOWN_SCROLL_STATE
        }
        scrollListener.onScrollStateChanged(null, listViewState)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager?

        val firstVisible = layoutManager?.findFirstVisibleItemPositions(null)?.first() ?: 0
        val visibleCount = abs(firstVisible - (layoutManager?.findLastVisibleItemPositions(null)?.first() ?: 0))
        val itemCount = recyclerView.adapter?.itemCount ?: 0

        if (firstVisible != lastFirstVisible || visibleCount != lastVisibleCount || itemCount != lastItemCount) {
            scrollListener.onScroll(null, firstVisible, visibleCount, itemCount)
            lastFirstVisible = firstVisible
            lastVisibleCount = visibleCount
            lastItemCount = itemCount
        }
    }

    companion object {

        const val UNKNOWN_SCROLL_STATE = Int.MIN_VALUE
    }

}