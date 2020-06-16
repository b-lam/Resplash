package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BasePagingSource

class CollectionPhotoPagingSource(
    private val photoService: PhotoService,
    private val collectionId: Int
) : BasePagingSource<Photo>() {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return photoService.getCollectionPhotos(
            id = collectionId,
            page = page,
            per_page = perPage
        )
    }
}