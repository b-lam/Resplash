package com.b_lam.resplash.ui.user

import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserPhotoFragment : PhotoFragment() {

    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val pagingDataAdapter =
        PhotoAdapter(itemEventCallback, false, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshPhotos() }
            photosNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            photosLiveData.observe(viewLifecycleOwner) { updatePagingData(it) }
        }
    }

    companion object {

        fun newInstance() = UserPhotoFragment()
    }
}
