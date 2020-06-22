package com.b_lam.resplash.ui.widget.recyclerview

import android.content.res.Configuration
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BasePagedListAdapter<T : Any>(
    diffCallback: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {

    var orientation = Configuration.ORIENTATION_PORTRAIT
}