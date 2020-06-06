package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BaseDataSourceFactory
import kotlinx.coroutines.CoroutineScope

class UserPhotoDataSourceFactory(
    private val userService: UserService,
    private val username: String,
    private val order: Order?,
    private val stats: Boolean,
    private val resolution: Resolution?,
    private val quantity: Int?,
    private val orientation: Orientation?,
    private val scope: CoroutineScope
) : BaseDataSourceFactory<Photo>() {

    override fun createDataSource() = UserPhotoDataSource(
        userService,
        username,
        order?.value,
        stats,
        resolution?.value,
        quantity,
        orientation?.value,
        scope
    )

    companion object {

        enum class Order(val value: String) {
            LATEST("latest"),
            OLDEST("oldest"),
            POPULAR("popular")
        }

        enum class Resolution(val value: String?) {
            DAYS("days")
        }

        enum class Orientation(val value: String?) {
            ALL(null),
            LANDSCAPE("landscape"),
            PORTRAIT("portrait"),
            SQUARISH("squarish")
        }
    }
}