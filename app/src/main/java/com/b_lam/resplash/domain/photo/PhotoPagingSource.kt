package com.b_lam.resplash.domain.photo

import androidx.annotation.StringRes
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BasePagingSource

class PhotoPagingSource(
    private val photoService: PhotoService,
    private val order: Order
) : BasePagingSource<Photo>() {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return photoService.getPhotos(
            page = page,
            per_page = perPage,
            order_by = order.value
        )
    }

    companion object {

        enum class Order(@StringRes val titleRes: Int, val value: String) {
            LATEST(R.string.order_latest, "latest"),
            OLDEST(R.string.order_oldest,"oldest"),
            POPULAR(R.string.order_popular, "popular")
        }
    }
}