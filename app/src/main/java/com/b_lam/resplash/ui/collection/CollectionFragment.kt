package com.b_lam.resplash.ui.collection

import android.content.Intent
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.collection.detail.CollectionDetailActivity
import com.b_lam.resplash.ui.user.UserActivity

abstract class CollectionFragment : BaseSwipeRecyclerViewFragment<Collection>() {

    abstract override val preloadPagedListAdapter: CollectionAdapter

    val itemEventCallback = object : CollectionAdapter.ItemEventCallback {

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

    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = ""

    override val itemSpacing: Int
        get() = resources.getDimensionPixelSize(R.dimen.keyline_7)
}
