package com.b_lam.resplash.ui.widget.recyclerview

import android.view.View
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_load_state_footer.view.*

class PagingLoadStateViewHolder(
    parent: View,
    private val retry: () -> Unit
): RecyclerView.ViewHolder(parent) {

    fun bind(loadState: LoadState) {
        with(itemView) {
            if (loadState is LoadState.Error) {
                retry_button.setOnClickListener { retry.invoke() }
                error_text_view.text = loadState.error.localizedMessage
            }
            progress_bar.isVisible = loadState is LoadState.Loading
            retry_button.isVisible = loadState !is LoadState.Loading
            error_text_view.isVisible = loadState !is LoadState.Loading
        }
    }
}