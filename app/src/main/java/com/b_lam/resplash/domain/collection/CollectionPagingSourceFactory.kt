package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.domain.BasePagingSourceFactory

class CollectionPagingSourceFactory(
    private val collectionService: CollectionService,
    private val order: CollectionPagingSource.Companion.Order
) : BasePagingSourceFactory<Collection>() {

    override fun createDataSource() = CollectionPagingSource(collectionService, order)
}
