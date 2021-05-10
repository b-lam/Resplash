package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.collection.CollectionService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class CollectionPhotoDataSourceFactory(
    private val collectionService: CollectionService,
    private val collectionId: String,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() = CollectionPhotoDataSource(collectionService, collectionId, scope)
}