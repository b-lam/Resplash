package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BasePagingSourceFactory

class UserCollectionPagingSourceFactory(
    private val userService: UserService,
    private val username: String
) : BasePagingSourceFactory<Collection>() {

    override fun createDataSource() = UserCollectionPagingSource(userService, username)
}
