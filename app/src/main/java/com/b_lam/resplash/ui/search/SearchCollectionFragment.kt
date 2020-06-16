package com.b_lam.resplash.ui.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchCollectionFragment : CollectionFragment() {

    private val sharedViewModel: SearchViewModel by sharedViewModel()

    private var searchJob: Job? = null

    override val pagingAdapter =
        CollectionAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override val emptyStateSubtitle: String
        get() = getString(R.string.no_search_results_subtitle)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.isEnabled = false
    }

    override fun observeEvents() {
        swipe_refresh_layout.setOnRefreshListener { pagingAdapter.refresh() }
        pagingAdapter.addLoadStateListener { updateLoadState(it) }
        sharedViewModel.queryLiveData.observe(viewLifecycleOwner) { query ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                sharedViewModel.searchCollections(query).collectLatest { pagingAdapter.submitData(it) }
            }
        }
    }

    companion object {

        fun newInstance() = SearchCollectionFragment()
    }
}