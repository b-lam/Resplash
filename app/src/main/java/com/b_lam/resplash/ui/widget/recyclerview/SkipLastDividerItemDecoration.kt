package com.b_lam.resplash.ui.widget.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Clone of [DividerItemDecoration] in Kotlin, which skips the last divider. The only two logical
 * changes in the class are indicated with a comment labeled "CHANGE".
 *
 * @constructor Creates a divider [RecyclerView.ItemDecoration] that can be used with a [LinearLayoutManager].
 * @param context Current context, it will be used to access resources.
 * @param orientation Divider orientation. Should be [.HORIZONTAL] or [.VERTICAL].
 */
class SkipLastDividerItemDecoration(context: Context, orientation: Int) : RecyclerView.ItemDecoration() {

    private var dividerDrawable: Drawable? = null

    /**
     * Current orientation. Either [.HORIZONTAL] or [.VERTICAL].
     */
    private var currentOrientation: Int = 0

    private val bounds = Rect()

    init {
        val styleArray = context.obtainStyledAttributes(listAttributes)
        dividerDrawable = styleArray.getDrawable(0)
        if (dividerDrawable == null) {
            Log.w(
                TAG,
                "@android:attr/listDivider was not set in the theme used for this DividerItemDecoration. " +
                        "Please set that attribute all call setDrawable()"
            )
        }
        styleArray.recycle()
        setOrientation(orientation)
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * [RecyclerView.LayoutManager] changes orientation.
     *
     * @param orientation [.HORIZONTAL] or [.VERTICAL]
     */
    fun setOrientation(orientation: Int) {
        require(!(orientation != HORIZONTAL && orientation != VERTICAL)) {
            "Invalid orientation. It should be either HORIZONTAL or VERTICAL"
        }
        currentOrientation = orientation
    }

    /**
     * Sets the [Drawable] for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    fun setDrawable(drawable: Drawable) {
        dividerDrawable = drawable
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null || dividerDrawable == null) {
            return
        }
        if (currentOrientation == VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int

        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = 0
            right = parent.width
        }

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) { // CHANGE: Added "-1"
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + child.translationY.roundToInt()
            val top = bottom - dividerDrawable!!.intrinsicHeight
            dividerDrawable!!.setBounds(left, top, right, bottom)
            dividerDrawable!!.draw(canvas)
        }
        canvas.restore()
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int

        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(
                parent.paddingLeft, top,
                parent.width - parent.paddingRight, bottom
            )
        } else {
            top = 0
            bottom = parent.height
        }

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) { // CHANGE: Added "-1"
            val child = parent.getChildAt(i)
            parent.layoutManager!!.getDecoratedBoundsWithMargins(child, bounds)
            val right = bounds.right + child.translationX.roundToInt()
            val left = right - dividerDrawable!!.intrinsicWidth
            dividerDrawable!!.setBounds(left, top, right, bottom)
            dividerDrawable!!.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (dividerDrawable == null) {
            outRect.set(0, 0, 0, 0)
            return
        }
        if (currentOrientation == VERTICAL) {
            outRect.set(0, 0, 0, dividerDrawable!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, dividerDrawable!!.intrinsicWidth, 0)
        }
    }

    companion object {
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val VERTICAL = LinearLayout.VERTICAL

        private const val TAG = "DividerItem"
        private val listAttributes = intArrayOf(android.R.attr.listDivider)
    }
}
