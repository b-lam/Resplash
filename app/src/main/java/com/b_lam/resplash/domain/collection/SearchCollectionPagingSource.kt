package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.domain.BasePagingSource

class SearchCollectionPagingSource(
    private val searchService: SearchService,
    private val query: String
) : BasePagingSource<Collection>() {

    override suspend fun getPage(page: Int, perPage: Int): List<Collection> {
        return searchService.searchCollections(
            query = query,
            page = page,
            per_page = perPage
        ).results
    }
}