package com.b_lam.resplash.ui.widget.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class StaggeredGridItemOffsetDecoration(
    private val spacing: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val params = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams
        val spanIndex = params?.spanIndex ?: 0
        val position = params?.absoluteAdapterPosition ?: 0

        outRect.left = if (spanIndex == 0) spacing else 0
        outRect.top = if (position < spanCount) spacing else 0
        outRect.right = spacing
        outRect.bottom = spacing
    }
}
