package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BasePagingSourceFactory

class UserLikesPagingSourceFactory(
    private val userService: UserService,
    private val username: String,
    private val order: UserLikesPagingSource.Companion.Order?,
    private val orientation: UserLikesPagingSource.Companion.Orientation?
) : BasePagingSourceFactory<Photo>() {

    override fun createDataSource() = UserLikesPagingSource(userService, username, order, orientation)
}
