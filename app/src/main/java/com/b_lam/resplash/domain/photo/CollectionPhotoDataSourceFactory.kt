package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class CollectionPhotoDataSourceFactory(
    private val photoService: PhotoService,
    private val collectionId: Int,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() = CollectionPhotoDataSource(photoService, collectionId, scope)
}