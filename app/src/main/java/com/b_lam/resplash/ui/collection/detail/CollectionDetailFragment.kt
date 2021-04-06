package com.b_lam.resplash.ui.collection.detail

import android.os.Bundle
import android.view.View
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import com.b_lam.resplash.util.livedata.observeOnce
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CollectionDetailFragment : PhotoFragment() {

    private val sharedViewModel: CollectionDetailViewModel by sharedViewModel()

    override val pagedListAdapter =
        PhotoAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.collectionLiveData.observeOnce(this) {
            binding.swipeRefreshLayout.isEnabled = sharedViewModel.isOwnCollection()
        }
    }

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshPhotos() }
            refreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            networkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = CollectionDetailFragment()
    }
}