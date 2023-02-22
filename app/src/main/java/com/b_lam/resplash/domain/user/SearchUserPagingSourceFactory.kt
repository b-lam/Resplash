package com.b_lam.resplash.domain.user

import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.BasePagingSourceFactory

class SearchUserPagingSourceFactory(
    private val searchService: SearchService,
    private val query: String
) : BasePagingSourceFactory<User>() {

    override fun createDataSource() = SearchUserPagingSource(searchService, query)
}
