package com.b_lam.resplash.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.FragmentSwipeRecyclerViewBinding
import com.b_lam.resplash.domain.PagingNetworkState
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.widget.recyclerview.BasePagingDataAdapter
import com.b_lam.resplash.util.RECYCLER_VIEW_CACHE_SIZE
import com.b_lam.resplash.util.scrollToTop
import com.b_lam.resplash.util.setupLayoutManager
import com.b_lam.resplash.util.showSnackBar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class BaseSwipeRecyclerViewFragment<T : Any, VH: RecyclerView.ViewHolder> :
    Fragment(R.layout.fragment_swipe_recycler_view) {

    val binding: FragmentSwipeRecyclerViewBinding by viewBinding()

    val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    abstract val pagingDataAdapter: BasePagingDataAdapter<T, VH>

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
            adapter = pagingDataAdapter.apply {
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
        pagingDataAdapter.orientation = newConfig.orientation
        pagingDataAdapter.notifyDataSetChanged()
    }

    fun scrollToTop() = binding.recyclerView.scrollToTop()

    fun updateNetworkState(networkState: PagingNetworkState) {
        when (networkState) {
            is PagingNetworkState.Refreshing -> showRefreshingState()
            is PagingNetworkState.Empty -> showEmptyState()
            is PagingNetworkState.Success -> showSuccessState()
            is PagingNetworkState.PageError ->
                binding.swipeRefreshLayout.showSnackBar(R.string.oops)
            is PagingNetworkState.RefreshError -> {
                binding.errorStateLayout.emptyErrorStateTitle.text = getString(R.string.error_state_title)
                binding.errorStateLayout.emptyErrorStateSubtitle.text = networkState.message
                showErrorState()
            }
        }
        binding.swipeRefreshLayout.isRefreshing =
            binding.swipeRefreshLayout.isRefreshing && networkState is PagingNetworkState.Refreshing
    }

    fun updatePagingData(pagingData: PagingData<T>) {
        lifecycleScope.launch {
            pagingDataAdapter.submitData(pagingData)
        }
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

    private fun showRefreshingState() {
        binding.recyclerView.isVisible = false
        binding.errorStateLayout.root.isVisible = false
        binding.emptyStateLayout.root.isVisible = false
        binding.contentLoadingLayout.show()
    }
}
