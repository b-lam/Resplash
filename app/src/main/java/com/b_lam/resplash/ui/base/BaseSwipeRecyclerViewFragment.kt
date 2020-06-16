package com.b_lam.resplash.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.BasePagingDataAdapter
import com.b_lam.resplash.ui.widget.recyclerview.PagingLoadStateAdapter
import com.b_lam.resplash.util.RECYCLER_VIEW_CACHE_SIZE
import com.b_lam.resplash.util.scrollToTop
import com.b_lam.resplash.util.setupLayoutManager
import com.b_lam.resplash.util.showSnackBar
import kotlinx.android.synthetic.main.empty_error_state_layout.view.*
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.android.ext.android.inject

abstract class BaseSwipeRecyclerViewFragment<T : Any> : BaseFragment() {

    val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    override val layoutId = R.layout.fragment_swipe_recycler_view

    abstract val pagingAdapter: BasePagingDataAdapter<T>

    abstract val emptyStateTitle: String

    abstract val emptyStateSubtitle: String

    abstract val itemSpacing: Int

    abstract fun observeEvents()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.apply {
            adapter = pagingAdapter.apply {
                layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
                orientation = resources.configuration.orientation
                withLoadStateFooter(PagingLoadStateAdapter { pagingAdapter.retry() })
            }
            setupLayoutManager(
                orientation = resources.configuration.orientation,
                layout = sharedPreferencesRepository.layout,
                spacing = itemSpacing
            )
            setItemViewCacheSize(RECYCLER_VIEW_CACHE_SIZE)
        }

        setEmptyStateText(emptyStateTitle, emptyStateSubtitle)

        observeEvents()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recycler_view.setupLayoutManager(
            orientation = newConfig.orientation,
            layout = sharedPreferencesRepository.layout,
            spacing = itemSpacing
        )
        pagingAdapter.orientation = newConfig.orientation
        pagingAdapter.notifyDataSetChanged()
    }

    fun scrollToTop() = recycler_view.scrollToTop()

    fun updateLoadState(loadState: CombinedLoadStates) {
        if (loadState.refresh !is LoadState.NotLoading) {
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    content_loading_layout.isVisible = true
                }
                is LoadState.Error -> {
                    error_state_layout.empty_error_state_title.text =
                        getString(R.string.error_state_title)
                    error_state_layout.empty_error_state_subtitle.text =
                        (loadState.refresh as LoadState.Error).error.localizedMessage
                    showErrorState()
                }
            }
        } else {
            showSuccessState()
            if (loadState.append is LoadState.Error) {
                swipe_refresh_layout.showSnackBar(R.string.oops)
            }
        }
        swipe_refresh_layout.isRefreshing =
            swipe_refresh_layout.isRefreshing && loadState.refresh is LoadState.Loading
    }

    private fun setEmptyStateText(title: String, subtitle: String) {
        empty_state_layout.empty_error_state_title.text = title
        empty_state_layout.empty_error_state_subtitle.text = subtitle
    }

    private fun showSuccessState() {
        recycler_view.isVisible = true
        error_state_layout.isVisible = false
        empty_state_layout.isVisible = false
        content_loading_layout.isVisible = false
    }

    private fun showErrorState() {
        recycler_view.isVisible = false
        error_state_layout.isVisible = true
        empty_state_layout.isVisible = false
        content_loading_layout.isVisible = false
    }

    private fun showEmptyState() {
        recycler_view.isVisible = false
        error_state_layout.isVisible = false
        empty_state_layout.isVisible = true
        content_loading_layout.isVisible = false
    }
}