package com.b_lam.resplash.util

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
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
    val layoutManager = layoutManager as? StaggeredGridLayoutManager
    layoutManager?.let {
        val firstVisibleItemPosition = it.findFirstVisibleItemPositions(null).first()
        if (firstVisibleItemPosition > 5) {
            scrollToPosition(5)
        }
    }
    smoothScrollToPosition(0)
}
