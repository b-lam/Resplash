package com.b_lam.resplash.ui.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchCollectionFragment : CollectionFragment() {

    private val sharedViewModel: SearchViewModel by sharedViewModel()

    override val pagedListAdapter =
        CollectionAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override val emptyStateSubtitle: String
        get() = getString(R.string.no_search_results_subtitle)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.isEnabled = false
    }

    override fun observeEvents() {
        with(sharedViewModel) {
            collectionsRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            collectionsNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            collectionsLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = SearchCollectionFragment()
    }
}