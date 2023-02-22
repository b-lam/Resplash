package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BasePagingSource

class UserCollectionPagingSource(
    private val userService: UserService,
    private val username: String
) : BasePagingSource<Collection>() {

    override suspend fun getPage(page: Int, perPage: Int): List<Collection> {
        return userService.getUserCollections(
            username = username,
            page = page,
            per_page = perPage
        )
    }
}
