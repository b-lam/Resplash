package com.b_lam.resplash.ui.main

import androidx.lifecycle.observe
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainPhotoFragment : PhotoFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    override val preloadPagedListAdapter =
        PhotoAdapter(context, itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            swipe_refresh_layout.setOnRefreshListener { refreshPhotos() }
            photosRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            photosNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    override fun onLikeClick(photo: Photo) {
        sharedViewModel.onLikeClick(photo)
    }

    companion object {

        fun newInstance() = MainPhotoFragment()
    }
}