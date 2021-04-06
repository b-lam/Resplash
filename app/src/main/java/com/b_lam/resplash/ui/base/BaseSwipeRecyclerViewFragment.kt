package com.b_lam.resplash.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.FragmentSwipeRecyclerViewBinding
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.BasePagedListAdapter
import com.b_lam.resplash.util.*
import org.koin.android.ext.android.inject

abstract class BaseSwipeRecyclerViewFragment<T : Any, VH: RecyclerView.ViewHolder> :
    Fragment(R.layout.fragment_swipe_recycler_view) {

    val binding: FragmentSwipeRecyclerViewBinding by viewBinding()

    val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    abstract val pagedListAdapter: BasePagedListAdapter<T, VH>

    abstract val emptyStateTitle: String

    abstract val emptyStateSubtitle: String

    abstract val itemSpacing: Int

    abstract fun observeEvents()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
            }
            adapter = pagedListAdapter.apply {
                orientation = resources.configuration.orientation
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
        binding.recyclerView.setupLayoutManager(
            orientation = newConfig.orientation,
            layout = sharedPreferencesRepository.layout,
            spacing = itemSpacing
        )
        pagedListAdapter.orientation = newConfig.orientation
        pagedListAdapter.notifyDataSetChanged()
    }

    fun scrollToTop() = binding.recyclerView.scrollToTop()

    fun updateRefreshState(refreshState: NetworkState) {
        when (refreshState) {
            is NetworkState.LOADING -> showLoadingState()
            is NetworkState.EMPTY -> showEmptyState()
            is NetworkState.ERROR -> {
                binding.errorStateLayout.emptyErrorStateTitle.text = getString(R.string.error_state_title)
                binding.errorStateLayout.emptyErrorStateSubtitle.text = refreshState.message
                showErrorState()
            }
        }
        binding.swipeRefreshLayout.isRefreshing =
            binding.swipeRefreshLayout.isRefreshing && refreshState is NetworkState.LOADING
    }

    fun updateNetworkState(networkState: NetworkState) {
        when (networkState) {
            is NetworkState.SUCCESS -> showSuccessState()
            is NetworkState.ERROR -> binding.swipeRefreshLayout.showSnackBar(R.string.oops)
        }
    }

    fun updatePagedList(pagedList: PagedList<T>) {
        pagedListAdapter.submitList(pagedList)
    }

    private fun setEmptyStateText(title: String, subtitle: String) {
        binding.emptyStateLayout.emptyErrorStateTitle.text = title
        binding.emptyStateLayout.emptyErrorStateSubtitle.text = subtitle
    }

    private fun showSuccessState() {
        binding.recyclerView.isVisible = true
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = false
        binding.contentLoadingLayout.hide()
    }

    private fun showErrorState() {
        binding.recyclerView.isVisible = false
        binding.errorStateLayout.root.isVisible = true
        binding.emptyStateLayout.root.isVisible = false
        binding.contentLoadingLayout.hide()
    }

    private fun showEmptyState() {
        binding.recyclerView.isVisible = false
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = true
        binding.contentLoadingLayout.hide()
    }

    private fun showLoadingState() {
        binding.recyclerView.isVisible = false
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = false
        binding.contentLoadingLayout.show()
    }
}