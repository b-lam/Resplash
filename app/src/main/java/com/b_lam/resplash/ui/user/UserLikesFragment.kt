package com.b_lam.resplash.ui.user

import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserLikesFragment : PhotoFragment() {
    
    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val pagingDataAdapter =
        PhotoAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshLikes() }
            likesNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            likesLiveData.observe(viewLifecycleOwner) { updatePagingData(it) }
        }
    }

    companion object {

        fun newInstance() = UserLikesFragment()
    }
}
