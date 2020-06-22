package com.b_lam.resplash.data.search.model

import com.b_lam.resplash.data.photo.model.Photo
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchPhotosResult(
    val total: Int,
    val total_pages: Int,
    val results: List<Photo>
)
