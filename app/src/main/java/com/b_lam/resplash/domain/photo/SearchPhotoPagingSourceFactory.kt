package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.domain.BasePagingSourceFactory

class SearchPhotoPagingSourceFactory(
    private val searchService: SearchService,
    private val query: String,
    private val order: SearchPhotoPagingSource.Companion.Order?,
    private val collections: String?,
    private val contentFilter: SearchPhotoPagingSource.Companion.ContentFilter?,
    private val color: SearchPhotoPagingSource.Companion.Color?,
    private val orientation: SearchPhotoPagingSource.Companion.Orientation?
) : BasePagingSourceFactory<Photo>() {

    override fun createDataSource() =
        SearchPhotoPagingSource(searchService, query, order, collections, contentFilter,
            color, orientation)
}
