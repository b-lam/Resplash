package com.b_lam.resplash.domain.photo

import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.UserService
import com.b_lam.resplash.domain.BasePagingSource

class UserPhotoPagingSource(
    private val userService: UserService,
    private val username: String,
    private val order: Order?,
    private val stats: Boolean?,
    private val resolution: Resolution?,
    private val quantity: Int?,
    private val orientation: Orientation?
) : BasePagingSource<Photo>() {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return userService.getUserPhotos(
            username = username,
            page = page,
            per_page = perPage,
            order_by = order?.value,
            stats = stats,
            resolution = resolution?.value,
            quantity = quantity,
            orientation = orientation?.value
        )
    }

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
