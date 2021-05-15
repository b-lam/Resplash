package com.b_lam.resplash.domain.topic

import com.b_lam.resplash.data.topic.TopicService
import com.b_lam.resplash.data.topic.model.Topic
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class TopicDataSourceFactory(
    private val topicService: TopicService,
    private val order: TopicDataSource.Companion.Order?,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Topic>() {

    override fun createDataSource() = TopicDataSource(topicService, order, scope)
}