package com.b_lam.resplash.ui.collection

import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment

abstract class CollectionFragment : BaseSwipeRecyclerViewFragment<Collection>() {

    abstract override val preloadPagedListAdapter: CollectionAdapter

    abstract val itemEventCallback: CollectionAdapter.ItemEventCallback

    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = ""

    override val itemSpacing: Int
        get() = resources.getDimensionPixelSize(R.dimen.keyline_7)
}
