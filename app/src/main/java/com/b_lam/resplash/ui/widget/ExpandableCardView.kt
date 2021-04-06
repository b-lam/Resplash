package com.b_lam.resplash.ui.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ExpandableCardViewBinding

class ExpandableCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ExpandableCardViewBinding by viewBinding(CreateMethod.INFLATE)

    lateinit var headerView: View
    lateinit var contentView: View

    @LayoutRes
    private var headerViewRes: Int = 0

    @LayoutRes
    private var contentViewRes: Int = 0

    private var slideAnimator: ValueAnimator? = null

    private var isExpanded = true

    private var isMoving: Boolean = false
    private var animDuration: Long

    private val defaultClickListener = OnClickListener {
        if (isExpanded) collapse() else expand()
    }

    private var listener: OnExpandChangeListener? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableCardView)
        headerViewRes =
            typedArray.getResourceId(R.styleable.ExpandableCardView_header_view, View.NO_ID)
        contentViewRes =
            typedArray.getResourceId(R.styleable.ExpandableCardView_content_view, View.NO_ID)
        isExpanded = typedArray.getBoolean(R.styleable.ExpandableCardView_expanded, false)
        animDuration = typedArray.getInteger(
            R.styleable.ExpandableCardView_animation_duration,
            DEFAULT_ANIMATION_DURATION
        ).toLong()
        typedArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        with(binding) {
            cardHeader.layoutResource = headerViewRes
            cardContent.layoutResource = contentViewRes
            headerView = cardHeader.inflate()
            contentView = cardContent.inflate()
        }

        initClickListeners()
    }

    private fun slideAnimator(start: Int, end: Int): ValueAnimator =
        ValueAnimator.ofInt(start, end).apply {
            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                val layoutParams = contentView.layoutParams
                layoutParams?.height = value
                contentView.layoutParams = layoutParams
            }
        }

    private fun expand(timeAnim: Long = animDuration) {
        if (isMoving) return
        listener?.onExpandChanged(true)
        isMoving = true
        contentView.visibility = View.VISIBLE

        contentView.measure(
            MeasureSpec.makeMeasureSpec(binding.root.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        val targetHeight = contentView.measuredHeight

        slideAnimator = slideAnimator(0, targetHeight).apply {
            onAnimationEnd {
                isExpanded = true
                isMoving = false
            }
            duration = timeAnim
            start()
        }
    }

    private fun collapse(timeAnim: Long = animDuration) {
        if (isMoving) return
        listener?.onExpandChanged(false)
        isMoving = true
        val finalHeight = contentView.height

        slideAnimator = slideAnimator(finalHeight, 0).apply {
            onAnimationEnd {
                contentView.visibility = View.GONE
                isExpanded = false
                isMoving = false
            }
            duration = timeAnim
            beforeCollapseStart()
            start()
        }
    }

    private fun beforeCollapseStart() {}

    fun removeOnExpandChangeListener() {
        this.listener = null
    }

    fun setOnExpandChangeListener(expandChangeListener: OnExpandChangeListener) {
        listener = expandChangeListener
    }

    fun setOnExpandChangeListener(expandChangeUnit: (Boolean) -> Unit) {
        listener = object : OnExpandChangeListener {
            override fun onExpandChanged(isExpanded: Boolean) {
                expandChangeUnit(isExpanded)
            }
        }
    }

    private fun ValueAnimator.onAnimationEnd(onAnimationEnd: () -> Unit) {
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                onAnimationEnd()
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }
        })
    }

    private fun initClickListeners() {
        val views = getViewsByTag(binding.root, "expand_or_collapse")
        views.forEach {
            it.setOnClickListener(defaultClickListener)
        }
    }

    private fun getViewsByTag(root: ViewGroup, tag: String): ArrayList<View> {
        val views = ArrayList<View>()
        val childCount = root.childCount
        for (i in 0 until childCount) {
            val child = root.getChildAt(i)
            if (child is ViewGroup) {
                views.addAll(getViewsByTag(child, tag))
            }

            val tagObj = child.tag
            if (tagObj != null && tagObj == tag) {
                views.add(child)
            }

        }
        return views
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = super.onSaveInstanceState()
        state?.let {
            val customViewSavedState =
                ExpandedCardSavedState(
                    state
                )
            customViewSavedState.isExpanded = isExpanded
            return customViewSavedState
        }
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val customViewSavedState = state as ExpandedCardSavedState
        isExpanded = customViewSavedState.isExpanded
        super.onRestoreInstanceState(customViewSavedState.superState)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isExpanded) {
            collapse(timeAnim = 0)
        }
    }

    private class ExpandedCardSavedState : BaseSavedState {

        var isExpanded: Boolean = false

        constructor(superState: Parcelable) : super(superState)

        private constructor(source: Parcel) : super(source) {
            isExpanded = source.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (isExpanded) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<ExpandedCardSavedState> {
            override fun createFromParcel(source: Parcel): ExpandedCardSavedState {
                return ExpandedCardSavedState(
                    source
                )
            }

            override fun newArray(size: Int): Array<ExpandedCardSavedState?> {
                return arrayOfNulls(size)
            }
        }

        override fun describeContents(): Int {
            return 0
        }
    }

    interface OnExpandChangeListener {
        fun onExpandChanged(isExpanded: Boolean)
    }

    companion object {

        private const val DEFAULT_ANIMATION_DURATION = 300
    }
}
