package com.b_lam.resplash.ui.collection.detail

import android.os.Bundle
import android.view.View
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import com.b_lam.resplash.util.livedata.observeOnce
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CollectionDetailFragment : PhotoFragment() {

    private val sharedViewModel: CollectionDetailViewModel by sharedViewModel()

    override val pagingDataAdapter =
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
            networkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagingData(it) }
        }
    }

    companion object {

        fun newInstance() = CollectionDetailFragment()
    }
}
