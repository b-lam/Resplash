package com.b_lam.resplash.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.widget.TextViewCompat
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.TopicStatusViewBinding

class TopicStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: TopicStatusViewBinding by viewBinding(CreateMethod.INFLATE)

    private var status: TopicStatus = TopicStatus.OPEN

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TopicStatusView)
        setStatus(typedArray.getEnum(R.styleable.TopicStatusView_status, TopicStatus.OPEN))
        typedArray.recycle()
    }

    fun setStatus(status: TopicStatus) {
        this.status = status

        with(binding.root) {
            text = context.getString(status.text)
            background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ContextCompat.getColor(context, status.backgroundColor),
                BlendModeCompat.SRC_ATOP
            )
            TextViewCompat.setCompoundDrawableTintList(this,
                ColorStateList.valueOf(ContextCompat.getColor(context, status.dotColor)))
        }

        invalidate()
        requestLayout()
    }

    private inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
        getInt(index, -1).let { if (it >= 0) enumValues<T>()[it] else default }

    companion object {

        fun String.toTopicStatus() = when (this) {
            "open" -> TopicStatus.OPEN
            else -> TopicStatus.CLOSED
        }

        enum class TopicStatus(
            @ColorRes val backgroundColor: Int,
            @ColorRes val dotColor: Int,
            @StringRes val text: Int)
        {
            OPEN(
                R.color.topic_status_open_background,
                R.color.topic_status_open_dot,
                R.string.topic_status_open),
            CLOSED(
                R.color.topic_status_closed_background,
                R.color.topic_status_closed_dot,
                R.string.topic_status_closed)
        }
    }
}