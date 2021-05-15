package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.topic.TopicService
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class TopicPhotoDataSourceFactory(
    private val topicService: TopicService,
    private val idOrSlug: String,
    private val orientation: TopicPhotoDataSource.Companion.Orientation?,
    private val order: TopicPhotoDataSource.Companion.Order?,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() =
        TopicPhotoDataSource(topicService, idOrSlug, orientation, order, scope)
}