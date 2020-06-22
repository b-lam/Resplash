package com.b_lam.resplash.domain.user

import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class SearchUserDataSourceFactory(
    private val searchService: SearchService,
    private val query: String,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<User>() {

    override fun createDataSource() = SearchUserDataSource(searchService, query, scope)
}