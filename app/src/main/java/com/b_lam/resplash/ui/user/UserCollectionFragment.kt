package com.b_lam.resplash.ui.user

import android.app.Activity
import android.content.Intent
import com.b_lam.resplash.ui.collection.CollectionAdapter
import com.b_lam.resplash.ui.collection.CollectionFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class UserCollectionFragment : CollectionFragment() {

    private val sharedViewModel: UserViewModel by sharedViewModel()

    override val pagingDataAdapter =
        CollectionAdapter(itemEventCallback, false, sharedPreferencesRepository)

    override fun observeEvents() {
        with(sharedViewModel) {
            binding.swipeRefreshLayout.setOnRefreshListener { refreshCollections() }
            collectionsNetworkStateLiveData.observe(viewLifecycleOwner) { updateNetworkState(it) }
            collectionsLiveData.observe(viewLifecycleOwner) { updatePagingData(it) }
        }
    }

    @Deprecated("Deprecated in Java")
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
