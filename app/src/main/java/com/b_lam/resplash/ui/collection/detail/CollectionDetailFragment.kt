package com.b_lam.resplash.ui.collection.detail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CollectionDetailFragment : PhotoFragment() {

    private val sharedViewModel: CollectionDetailViewModel by sharedViewModel()

    override val preloadPagedListAdapter =
        PhotoAdapter(context, itemEventCallback, true, sharedPreferencesRepository)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipe_refresh_layout.isEnabled = false
    }

    override fun observeEvents() {
        with(sharedViewModel) {
            refreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            networkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = CollectionDetailFragment()
    }
}