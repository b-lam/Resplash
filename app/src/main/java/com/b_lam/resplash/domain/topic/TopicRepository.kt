package com.b_lam.resplash.domain.topic

import com.b_lam.resplash.data.topic.TopicService
import com.b_lam.resplash.data.topic.model.Topic
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class TopicRepository(
    private val topicService: TopicService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun getTopics(
        order: TopicDataSource.Companion.Order,
        scope: CoroutineScope
    ): Listing<Topic> {
        return TopicDataSourceFactory(topicService, order, scope).createListing()
    }

    suspend fun getTopic(idOrSlug: String) = safeApiCall(dispatcher) {
        topicService.getTopic(idOrSlug)
    }
}