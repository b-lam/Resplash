package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class CollectionDataSourceFactory(
    private val collectionService: CollectionService,
    private val order: Order,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Collection>() {

    override fun createDataSource() = CollectionDataSource(collectionService, order, scope)

    companion object {

        enum class Order(val titleRes: Int, val value: String) {
            ALL(R.string.order_all, "all"),
            FEATURED(R.string.order_featured, "featured")
        }
    }
}