package com.b_lam.resplash.domain.collection

import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class UserCollectionDataSourceFactory(
    private val userService: UserService,
    private val username: String,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Collection>() {

    override fun createDataSource() = UserCollectionDataSource(userService, username, scope)
}