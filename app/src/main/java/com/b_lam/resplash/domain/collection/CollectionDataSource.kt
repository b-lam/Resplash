package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class CollectionDataSource(
    private val collectionService: CollectionService,
    private val order: CollectionDataSourceFactory.Companion.Order,
    scope: CoroutineScope
) : BaseDataSource<Collection>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Collection> {
        return when (order) {
            CollectionDataSourceFactory.Companion.Order.ALL -> collectionService.getAllCollections(page, perPage)
            CollectionDataSourceFactory.Companion.Order.FEATURED -> collectionService.getFeaturedCollections(page, perPage)
        }
    }
}