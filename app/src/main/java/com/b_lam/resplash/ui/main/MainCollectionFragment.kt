package com.b_lam.resplash.ui.main

import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainCollectionFragment : CollectionFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    override val pagedListAdapter =
        CollectionAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshCollections() }
            collectionsRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            collectionsNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            collectionsLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = MainCollectionFragment()
    }
}