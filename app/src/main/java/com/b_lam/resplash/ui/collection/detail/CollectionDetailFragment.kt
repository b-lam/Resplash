package com.b_lam.resplash.ui.collection.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import com.b_lam.resplash.util.livedata.observeOnce
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CollectionDetailFragment : PhotoFragment() {

    private val sharedViewModel: CollectionDetailViewModel by sharedViewModel()

    override val pagedListAdapter =
        PhotoAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.collectionLiveData.observeOnce(this) {
            swipe_refresh_layout.isEnabled = sharedViewModel.isOwnCollection()
        }
    }

    override fun observeEvents() {
        with(sharedViewModel) {
            swipe_refresh_layout.setOnRefreshListener { refreshPhotos() }
            refreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            networkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    override fun trackDownload(id: String) {
        sharedViewModel.trackDownload(id)
    }

    companion object {

        fun newInstance() = CollectionDetailFragment()
    }
}