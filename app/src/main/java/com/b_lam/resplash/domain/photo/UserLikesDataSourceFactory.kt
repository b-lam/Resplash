package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class UserLikesDataSourceFactory(
    private val userService: UserService,
    private val username: String,
    private val order: UserLikesDataSource.Companion.Order?,
    private val orientation: UserLikesDataSource.Companion.Orientation?,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() = UserLikesDataSource(userService, username, order,
        orientation, scope)
}