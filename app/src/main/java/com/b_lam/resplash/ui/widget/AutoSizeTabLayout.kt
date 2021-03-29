package com.b_lam.resplash.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.b_lam.resplash.R
import com.google.android.material.tabs.TabLayout

class AutoSizeTabLayout(context: Context, attrs: AttributeSet) : TabLayout(context, attrs) {

    override fun newTab(): Tab {
        return super.newTab().apply {
            setCustomView(R.layout.auto_size_tab_text)
        }
    }
}