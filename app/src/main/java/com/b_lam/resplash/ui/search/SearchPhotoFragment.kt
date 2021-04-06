package com.b_lam.resplash.ui.search

import android.os.Bundle
import android.view.View
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchPhotoFragment : PhotoFragment() {
    
    private val sharedViewModel: SearchViewModel by sharedViewModel()

    override val pagedListAdapter =
        PhotoAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override val emptyStateSubtitle: String
        get() = getString(R.string.no_search_results_subtitle)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.isEnabled = false
    }

    override fun observeEvents() {
        with(sharedViewModel) {
            photosRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            photosNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = SearchPhotoFragment()
    }
}