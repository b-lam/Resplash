package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class CollectionPhotoDataSource(
    private val collectionService: CollectionService,
    private val collectionId: Int,
    scope: CoroutineScope
) : BaseDataSource<Photo>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return collectionService.getCollectionPhotos(
            id = collectionId,
            page = page,
            per_page = perPage
        )
    }
}