package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.PhotoService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class PhotoDataSourceFactory(
    private val photoService: PhotoService,
    private val order: Order,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() = PhotoDataSource(photoService, order, scope)

    companion object {

        enum class Order(val titleRes: Int, val value: String) {
            LATEST(R.string.order_latest, "latest"),
            OLDEST(R.string.order_oldest,"oldest"),
            POPULAR(R.string.order_popular, "popular")
        }
    }
}