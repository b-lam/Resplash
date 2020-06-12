package com.b_lam.resplash.ui.user

import androidx.lifecycle.observe
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserPhotoFragment : PhotoFragment() {

    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val preloadPagedListAdapter =
        PhotoAdapter(context, itemEventCallback, false, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            swipe_refresh_layout.setOnRefreshListener { refreshPhotos() }
            photosRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            photosNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    override fun trackDownload(id: String) {
        sharedViewModel.trackDownload(id)
    }

    companion object {

        fun newInstance() = UserPhotoFragment()
    }
}