package com.b_lam.resplash.ui.search

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.photo.detail.PhotoDetailActivity
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.ui.user.UserAdapter
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchUserFragment : BaseSwipeRecyclerViewFragment<User>() {

    private val sharedViewModel: SearchViewModel by sharedViewModel()

    private val itemEventCallback = object : UserAdapter.ItemEventCallback {

        override fun onUserClick(user: User) {
            Intent(context, UserActivity::class.java).apply {
                putExtra(UserActivity.EXTRA_USER, user)
                startActivity(this)
            }
        }

        override fun onPhotoClick(photo: Photo) {
            Intent(context, PhotoDetailActivity::class.java).apply {
                putExtra(PhotoDetailActivity.EXTRA_PHOTO, photo)
                startActivity(this)
            }
        }
    }

    override val pagedListAdapter = UserAdapter(itemEventCallback)

    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = getString(R.string.no_search_results_subtitle)

    override val itemSpacing: Int
        get() = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipe_refresh_layout.isEnabled = false
    }

    override fun observeEvents() {
        with(sharedViewModel) {
            usersRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            usersNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            usersLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onConfigurationChanged(newConfig: Configuration) {
        // Do nothing
    }

    companion object {

        fun newInstance() = SearchUserFragment()
    }
}
