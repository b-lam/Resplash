package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.domain.BasePagingSourceFactory

class SearchCollectionPagingSourceFactory(
    private val searchService: SearchService,
    private val query: String
) : BasePagingSourceFactory<Collection>() {

    override fun createDataSource() = SearchCollectionPagingSource(searchService, query)
}
