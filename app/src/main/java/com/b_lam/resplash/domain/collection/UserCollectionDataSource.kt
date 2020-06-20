package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class UserCollectionDataSource(
    private val userService: UserService,
    private val username: String,
    scope: CoroutineScope
) : BaseDataSource<Collection>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Collection> {
        return userService.getUserCollections(
            username = username,
            page = page,
            per_page = perPage
        )
    }
}