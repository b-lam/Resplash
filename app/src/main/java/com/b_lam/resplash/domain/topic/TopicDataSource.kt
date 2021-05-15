package com.b_lam.resplash.domain.topic

import androidx.annotation.StringRes
import com.b_lam.resplash.R
import com.b_lam.resplash.data.topic.TopicService
import com.b_lam.resplash.data.topic.model.Topic
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class TopicDataSource(
    private val topicService: TopicService,
    private val order: Order?,
    scope: CoroutineScope
) : BaseDataSource<Topic>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Topic> {
        return topicService.getTopics(
            ids = null,
            page = page,
            per_page = perPage,
            order_by = order?.value
        )
    }

    companion object {

        enum class Order(@StringRes val titleRes: Int, val value: String) {
            POSITION(R.string.order_all, "position"),
            FEATURED(R.string.order_all, "featured"),
            LATEST(R.string.order_all, "latest"),
            OLDEST(R.string.order_all, "oldest"),
        }
    }
}