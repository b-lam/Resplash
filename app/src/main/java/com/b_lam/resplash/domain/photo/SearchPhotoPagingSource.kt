package com.b_lam.resplash.domain.photo

import androidx.annotation.StringRes
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.search.SearchService
import com.b_lam.resplash.domain.BasePagingSource

class SearchPhotoPagingSource(
    private val searchService: SearchService,
    private val query: String,
    private val order: Order?,
    private val collections: String?,
    private val contentFilter: ContentFilter?,
    private val color: Color?,
    private val orientation: Orientation?
) : BasePagingSource<Photo>() {

    override suspend fun getPage(page: Int, perPage: Int): List<Photo> {
        return searchService.searchPhotos(
            query = query,
            page = page,
            per_page = perPage,
            order_by = order?.value,
            collections = collections,
            contentFilter = contentFilter?.value,
            color = color?.value,
            orientation = orientation?.value
        ).results
    }

    companion object {

        enum class Order(val value: String) {
            LATEST("latest"),
            RELEVANT("relevant")
        }

        enum class ContentFilter(val value: String) {
            LOW("low"),
            HIGH("high")
        }

        enum class Color(@StringRes val titleRes: Int, val value: String?) {
            ANY(R.string.filter_color_any, null),
            BLACK_AND_WHITE(R.string.filter_color_black_white, "black_and_white"),
            BLACK(R.string.filter_color_black, "black"),
            WHITE(R.string.filter_color_white, "white"),
            YELLOW(R.string.filter_color_yellow, "yellow"),
            ORANGE(R.string.filter_color_orange, "orange"),
            RED(R.string.filter_color_red, "red"),
            PURPLE(R.string.filter_color_purple, "purple"),
            MAGENTA(R.string.filter_color_magenta, "magenta"),
            GREEN(R.string.filter_color_green, "green"),
            TEAL(R.string.filter_color_teal, "teal"),
            BLUE(R.string.filter_color_blue, "blue")
        }

        enum class Orientation(val value: String?) {
            ANY(null),
            LANDSCAPE("landscape"),
            PORTRAIT("portrait"),
            SQUARISH("squarish")
        }
    }
}
