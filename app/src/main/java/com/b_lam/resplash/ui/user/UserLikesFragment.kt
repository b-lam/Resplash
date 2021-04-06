package com.b_lam.resplash.ui.user

import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserLikesFragment : PhotoFragment() {
    
    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val pagedListAdapter =
        PhotoAdapter(itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshLikes() }
            likesRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            likesNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            likesLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = UserLikesFragment()
    }
}