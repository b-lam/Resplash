package com.b_lam.resplash.domain.photo

import androidx.annotation.StringRes
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.topic.TopicService
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class TopicPhotoDataSource(
    private val topicService: TopicService,
    private val idOrSlug: String,
    private val orientation: Orientation?,
    private val order: Order?,
    scope: CoroutineScope
) : BaseDataSource<Photo>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return topicService.getTopicPhotos(
            id_or_slug = idOrSlug,
            page = page,
            per_page = perPage,
            orientation = orientation?.value,
            order_by = order?.value
        )
    }

    companion object {

        enum class Orientation(val value: String?) {
            ANY(null),
            LANDSCAPE("landscape"),
            PORTRAIT("portrait"),
            SQUARISH("squarish")
        }

        enum class Order(@StringRes val titleRes: Int, val value: String) {
            LATEST(R.string.order_latest, "latest"),
            OLDEST(R.string.order_oldest,"oldest"),
            POPULAR(R.string.order_popular, "popular")
        }
    }
}