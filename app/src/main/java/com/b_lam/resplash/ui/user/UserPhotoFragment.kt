package com.b_lam.resplash.ui.user

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.b_lam.resplash.ui.photo.PhotoAdapter
import com.b_lam.resplash.ui.photo.PhotoFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserPhotoFragment : PhotoFragment() {

    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val pagingAdapter =
        PhotoAdapter(itemEventCallback, false, sharedPreferencesRepository)

    override fun observeEvents() {
        swipe_refresh_layout.setOnRefreshListener { pagingAdapter.refresh() }
        pagingAdapter.addLoadStateListener { updateLoadState(it) }
        sharedViewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            user.username?.let { username ->
                lifecycleScope.launch {
                    sharedViewModel.getUserPhotos(username).collectLatest {
                        pagingAdapter.submitData(it)
                    }
                }
            }
        }
    }

    override fun trackDownload(id: String) {
        sharedViewModel.trackDownload(id)
    }

    companion object {

        fun newInstance() = UserPhotoFragment()
    }
}