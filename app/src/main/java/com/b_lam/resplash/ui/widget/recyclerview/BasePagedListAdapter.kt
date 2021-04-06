package com.b_lam.resplash.ui.widget.recyclerview

import android.content.res.Configuration
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BasePagedListAdapter<T : Any, VH: RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, VH>(diffCallback) {

    var orientation = Configuration.ORIENTATION_PORTRAIT
}