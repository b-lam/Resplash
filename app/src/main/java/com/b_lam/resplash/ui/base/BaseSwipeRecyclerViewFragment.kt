package com.b_lam.resplash.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.BasePagedListAdapter
import com.b_lam.resplash.util.*
import kotlinx.android.synthetic.main.empty_error_state_layout.view.*
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.android.ext.android.inject

abstract class BaseSwipeRecyclerViewFragment<T> : BaseFragment() {

    val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    override val layoutId = R.layout.fragment_swipe_recycler_view

    abstract val pagedListAdapter: BasePagedListAdapter<T>

    abstract val emptyStateTitle: String

    abstract val emptyStateSubtitle: String

    abstract val itemSpacing: Int

    abstract fun observeEvents()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.apply {
            adapter = pagedListAdapter.apply {
                orientation = resources.configuration.orientation
            }
            layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
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
        pagedListAdapter.orientation = newConfig.orientation
        pagedListAdapter.notifyDataSetChanged()
    }

    fun scrollToTop() = recycler_view.scrollToTop()

    fun updateRefreshState(refreshState: NetworkState) {
        when (refreshState) {
            is NetworkState.LOADING -> showLoadingState()
            is NetworkState.EMPTY -> showEmptyState()
            is NetworkState.ERROR -> {
                error_state_layout.empty_error_state_title.text = getString(R.string.error_state_title)
                error_state_layout.empty_error_state_subtitle.text = refreshState.message
                showErrorState()
            }
        }
        swipe_refresh_layout.isRefreshing = swipe_refresh_layout.isRefreshing && refreshState is NetworkState.LOADING
    }

    fun updateNetworkState(networkState: NetworkState) {
        when (networkState) {
            is NetworkState.SUCCESS -> showSuccessState()
            is NetworkState.ERROR -> swipe_refresh_layout.showSnackBar(R.string.oops)
        }
    }

    fun updatePagedList(pagedList: PagedList<T>) {
        pagedListAdapter.submitList(pagedList)
    }

    private fun setEmptyStateText(title: String, subtitle: String) {
        empty_state_layout.empty_error_state_title.text = title
        empty_state_layout.empty_error_state_subtitle.text = subtitle
    }

    private fun showSuccessState() {
        recycler_view.isVisible = true
        error_state_layout.isVisible = false
        empty_state_layout.isVisible = false
        content_loading_layout.hide()
    }

    private fun showErrorState() {
        recycler_view.isVisible = false
        error_state_layout.isVisible = true
        empty_state_layout.isVisible = false
        content_loading_layout.hide()
    }

    private fun showEmptyState() {
        recycler_view.isVisible = false
        error_state_layout.isVisible = false
        empty_state_layout.isVisible = true
        content_loading_layout.hide()
    }

    private fun showLoadingState() {
        recycler_view.isVisible = false
        error_state_layout.isVisible = false
        empty_state_layout.isVisible = false
        content_loading_layout.show()
    }
}