package com.b_lam.resplash.ui.main

import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainCollectionFragment : CollectionFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    override val pagingDataAdapter =
        CollectionAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshCollections() }
            collectionsNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            collectionsLiveData.observe(viewLifecycleOwner) { updatePagingData(it) }
        }
    }

    companion object {

        fun newInstance() = MainCollectionFragment()
    }
}
