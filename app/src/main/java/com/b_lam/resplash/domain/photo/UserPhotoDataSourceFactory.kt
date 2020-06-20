package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class UserPhotoDataSourceFactory(
    private val userService: UserService,
    private val username: String,
    private val order: UserPhotoDataSource.Companion.Order?,
    private val stats: Boolean,
    private val resolution: UserPhotoDataSource.Companion.Resolution?,
    private val quantity: Int?,
    private val orientation: UserPhotoDataSource.Companion.Orientation?,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() = UserPhotoDataSource(userService, username, order,
        stats, resolution, quantity, orientation, scope)
}