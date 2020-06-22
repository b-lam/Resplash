package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class PhotoDataSourceFactory(
    private val photoService: PhotoService,
    private val order: PhotoDataSource.Companion.Order,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() = PhotoDataSource(photoService, order, scope)
}