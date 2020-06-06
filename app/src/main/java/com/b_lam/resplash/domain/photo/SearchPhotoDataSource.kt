package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class SearchPhotoDataSource(
    private val searchService: SearchService,
    private val query: String,
    private val order: String?,
    private val collections: String?,
    private val contentFilter: String?,
    private val color: String?,
    private val orientation: String?,
    scope: CoroutineScope
) : BaseDataSource<Photo>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return searchService.searchPhotos(
            query = query,
            page = page,
            per_page = perPage,
            order_by = order,
            collections = collections,
            contentFilter = contentFilter,
            color = color,
            orientation = orientation
        ).results
    }
}