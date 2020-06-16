package com.b_lam.resplash.ui.search

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.photo.detail.PhotoDetailActivity
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.ui.user.UserAdapter
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SearchUserFragment : BaseSwipeRecyclerViewFragment<User>() {

    private val sharedViewModel: SearchViewModel by sharedViewModel()

    private var searchJob: Job? = null

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

    override val pagingAdapter = UserAdapter(itemEventCallback)

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
        swipe_refresh_layout.setOnRefreshListener { pagingAdapter.refresh() }
        pagingAdapter.addLoadStateListener { updateLoadState(it) }
        sharedViewModel.queryLiveData.observe(viewLifecycleOwner) { query ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                sharedViewModel.searchUsers(query).collectLatest { pagingAdapter.submitData(it) }
            }
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
