package com.b_lam.resplash.ui.user

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import kotlinx.android.synthetic.main.fragment_swipe_recycler_view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserCollectionFragment : CollectionFragment() {

    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val pagingAdapter =
        CollectionAdapter(itemEventCallback, false, sharedPreferencesRepository)

    override fun observeEvents() {
        swipe_refresh_layout.setOnRefreshListener { pagingAdapter.refresh() }
        pagingAdapter.addLoadStateListener { updateLoadState(it) }
        sharedViewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            user.username?.let { username ->
                lifecycleScope.launch {
                    sharedViewModel.getUserCollections(username).collectLatest {
                        pagingAdapter.submitData(it)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE_USER_COLLECTION_UPDATE && resultCode == Activity.RESULT_OK) {
            if (data?.getBooleanExtra(EXTRA_USER_COLLECTION_DELETE_FLAG, false) == true ||
                data?.getBooleanExtra(EXTRA_USER_COLLECTION_MODIFY_FLAG, false) == true) {
                pagingAdapter.refresh()
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