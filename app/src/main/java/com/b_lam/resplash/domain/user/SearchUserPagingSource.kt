package com.b_lam.resplash.domain.user

import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.BasePagingSource

class SearchUserPagingSource(
    private val searchService: SearchService,
    private val query: String
) : BasePagingSource<User>() {

    override suspend fun getPage(page: Int, perPage: Int): List<User> {
        return searchService.searchUsers(
            query = query,
            page = page,
            per_page = perPage
        ).results
    }
}