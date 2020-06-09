package com.b_lam.resplash.ui.main

import android.content.Intent
import androidx.lifecycle.observe
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import com.b_lam.resplash.ui.collection.detail.CollectionDetailActivity
import com.b_lam.resplash.ui.user.UserActivity
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainCollectionFragment : CollectionFragment() {

    private val sharedViewModel: MainViewModel by sharedViewModel()

    override val itemEventCallback = object : CollectionAdapter.ItemEventCallback {

        override fun onCollectionClick(collection: Collection) {
            val intent = Intent(context, CollectionDetailActivity::class.java).apply {
                putExtra(CollectionDetailActivity.EXTRA_COLLECTION, collection)
            }
            startActivity(intent)
        }

        override fun onUserClick(user: User) {
            Intent(context, UserActivity::class.java).apply {
                putExtra(UserActivity.EXTRA_USER, user)
                startActivity(this)
            }
        }
    }

    override val preloadPagedListAdapter =
        CollectionAdapter(context, itemEventCallback, true, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            swipe_refresh_layout.setOnRefreshListener { refreshCollections() }
            collectionsRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            collectionsNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            collectionsLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    companion object {

        fun newInstance() = MainCollectionFragment()
    }
}