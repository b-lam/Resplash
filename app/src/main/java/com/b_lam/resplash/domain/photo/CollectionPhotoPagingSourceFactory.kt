package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BasePagingSourceFactory

class CollectionPhotoPagingSourceFactory(
    private val collectionService: CollectionService,
    private val collectionId: String
) : BasePagingSourceFactory<Photo>() {

    override fun createDataSource() = CollectionPhotoPagingSource(collectionService, collectionId)
}
