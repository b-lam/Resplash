package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BasePagingSourceFactory

class UserPhotoPagingSourceFactory(
    private val userService: UserService,
    private val username: String,
    private val order: UserPhotoPagingSource.Companion.Order?,
    private val stats: Boolean,
    private val resolution: UserPhotoPagingSource.Companion.Resolution?,
    private val quantity: Int?,
    private val orientation: UserPhotoPagingSource.Companion.Orientation?
) : BasePagingSourceFactory<Photo>() {

    override fun createDataSource() = UserPhotoPagingSource(userService, username, order,
        stats, resolution, quantity, orientation)
}
