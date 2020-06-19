package com.b_lam.resplash.util

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.b_lam.resplash.ui.widget.recyclerview.StaggeredGridItemOffsetDecoration

const val RECYCLER_VIEW_CACHE_SIZE = 4

const val LAYOUT_DEFAULT = "default"
const val LAYOUT_MINIMAL = "minimal"
const val LAYOUT_GRID = "grid"

fun RecyclerView.setupLayoutManager(
    orientation: Int,
    layout: String?,
    spacing: Int
) {
    val decorationIndex = 0
    val spanCount =
        if (layout == LAYOUT_GRID || orientation == ORIENTATION_LANDSCAPE) { 2 } else { 1 }

    (layoutManager as? StaggeredGridLayoutManager)?.spanCount = spanCount

    if (itemDecorationCount != 0) { removeItemDecorationAt(decorationIndex) }

    if (layout != LAYOUT_MINIMAL || orientation == ORIENTATION_LANDSCAPE) {
        addItemDecoration(
            StaggeredGridItemOffsetDecoration(
                spacing,
                spanCount
            ),
            decorationIndex
        )
    }
}

fun RecyclerView.scrollToTop() {
    layoutManager?.let {
        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
        if (firstVisibleItemPosition > 6) {
            scrollToPosition(6)
        }
        smoothScrollToPosition(0)
    }
}

private fun RecyclerView.LayoutManager?.findFirstVisibleItemPosition(): Int {
    return if (this is LinearLayoutManager) {
        findFirstVisibleItemPosition()
    } else if (this is GridLayoutManager) {
        findFirstVisibleItemPosition()
    } else if (this is StaggeredGridLayoutManager) {
        findFirstVisibleItemPositions(null).first()
    } else {
        -1
    }
}
