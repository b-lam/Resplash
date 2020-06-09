package com.b_lam.resplash.ui.user

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.observe
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import com.b_lam.resplash.ui.collection.detail.CollectionDetailActivity
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserCollectionFragment : CollectionFragment() {

    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val itemEventCallback = object : CollectionAdapter.ItemEventCallback {

        override fun onCollectionClick(collection: Collection) {
            val intent = Intent(context, CollectionDetailActivity::class.java).apply {
                putExtra(CollectionDetailActivity.EXTRA_COLLECTION, collection)
                putExtra(CollectionDetailActivity.EXTRA_IS_USER_COLLECTION, sharedViewModel.isPersonalProfile())
            }
            startActivityForResult(intent, RESULT_CODE_USER_COLLECTION_UPDATE)
        }

        override fun onUserClick(user: User) {
            Intent(context, UserActivity::class.java).apply {
                putExtra(UserActivity.EXTRA_USER, user)
                startActivity(this)
            }
        }
    }

    override val preloadPagedListAdapter =
        CollectionAdapter(context, itemEventCallback, false, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            swipe_refresh_layout.setOnRefreshListener { refreshCollections() }
            collectionsRefreshStateLiveData.observe(viewLifecycleOwner) { updateRefreshState(it) }
            collectionsNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            collectionsLiveData.observe(viewLifecycleOwner) { updatePagedList(it) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE_USER_COLLECTION_UPDATE && resultCode == Activity.RESULT_OK) {
            if (data?.getBooleanExtra(EXTRA_USER_COLLECTION_DELETE_FLAG, false) == true ||
                data?.getBooleanExtra(EXTRA_USER_COLLECTION_MODIFY_FLAG, false) == true) {
                sharedViewModel.refreshCollections()
            }
        }
    }

    companion object {

        const val RESULT_CODE_USER_COLLECTION_UPDATE = 0
        const val EXTRA_USER_COLLECTION_DELETE_FLAG = "extra_user_collection_delete_flag"
        const val EXTRA_USER_COLLECTION_MODIFY_FLAG = "extra_user_collection_modify_flag"

        fun newInstance() = UserCollectionFragment()
    }
}