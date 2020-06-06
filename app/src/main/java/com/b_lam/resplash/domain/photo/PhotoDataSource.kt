package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class PhotoDataSource(
    private val photoService: PhotoService,
    private val order: PhotoDataSourceFactory.Companion.Order,
    scope: CoroutineScope
) : BaseDataSource<Photo>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return photoService.getPhotos(
            page = page,
            per_page = perPage,
            order_by = order.value
        )
    }
}