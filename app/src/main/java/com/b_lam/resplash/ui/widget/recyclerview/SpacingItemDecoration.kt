package com.b_lam.resplash.ui.widget.recyclerview

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(
    context: Context,
    @DimenRes spacingId: Int,
    val orientation: Int = RecyclerView.VERTICAL
) : RecyclerView.ItemDecoration() {

    private val spacing: Int = context.resources.getDimensionPixelSize(spacingId)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)

        if (orientation == RecyclerView.VERTICAL) {
            outRect.apply {
                left = spacing
                right = spacing
                bottom = spacing
                if (position == 0) top = spacing
            }
        } else if (orientation == RecyclerView.HORIZONTAL) {
            outRect.apply {
                right = spacing
                top = spacing
                bottom = spacing
                if (position == 0) left = spacing
            }
        }
    }
}
