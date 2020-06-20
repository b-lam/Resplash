package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class CollectionDataSourceFactory(
    private val collectionService: CollectionService,
    private val order: CollectionDataSource.Companion.Order,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Collection>() {

    override fun createDataSource() = CollectionDataSource(collectionService, order, scope)
}