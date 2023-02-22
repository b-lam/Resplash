package com.b_lam.resplash.ui.widget.recyclerview

import android.content.res.Configuration
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BasePagingDataAdapter<T : Any, VH: RecyclerView.ViewHolder>(
    diffCallback: DiffUtil.ItemCallback<T>
) : PagingDataAdapter<T, VH>(diffCallback) {

    var orientation = Configuration.ORIENTATION_PORTRAIT
}
