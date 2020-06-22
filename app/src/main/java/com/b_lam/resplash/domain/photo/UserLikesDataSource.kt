package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BaseDataSource
import kotlinx.coroutines.CoroutineScope

class UserLikesDataSource(
    private val userService: UserService,
    private val username: String,
    private val order: Order?,
    private val orientation: Orientation?,
    scope: CoroutineScope
) : BaseDataSource<Photo>(scope) {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return userService.getUserLikes(
            username = username,
            page = page,
            per_page = perPage,
            order_by = order?.value,
            orientation = orientation?.value
        )
    }

    companion object {

        enum class Order(val value: String) {
            LATEST("latest"),
            OLDEST("oldest"),
            POPULAR("popular")
        }

        enum class Orientation(val value: String?) {
            ALL(null),
            LANDSCAPE("landscape"),
            PORTRAIT("portrait"),
            SQUARISH("squarish")
        }
    }
}