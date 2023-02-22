package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BasePagingSourceFactory

class PhotoPagingSourceFactory(
    private val photoService: PhotoService,
    private val order: PhotoPagingSource.Companion.Order
) : BasePagingSourceFactory<Photo>() {

    override fun createDataSource() = PhotoPagingSource(photoService, order)
}
