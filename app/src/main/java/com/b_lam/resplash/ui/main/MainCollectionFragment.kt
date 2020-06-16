package com.b_lam.resplash.ui.main

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainCollectionFragment : CollectionFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    private var job: Job? = null

    override val pagingAdapter =
        CollectionAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        swipe_refresh_layout.setOnRefreshListener { pagingAdapter.refresh() }
        pagingAdapter.addLoadStateListener { updateLoadState(it) }
        sharedViewModel.collectionOrderLiveData.observe(viewLifecycleOwner) { order ->
            job?.cancel()
            job = lifecycleScope.launch {
                sharedViewModel.getCollections(order).collectLatest { pagingAdapter.submitData(it) }
            }
        }
    }

    companion object {

        fun newInstance() = MainCollectionFragment()
    }
}