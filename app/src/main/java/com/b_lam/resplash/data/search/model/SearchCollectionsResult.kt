package com.b_lam.resplash.data.search.model

import com.b_lam.resplash.data.collection.model.Collection
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchCollectionsResult(
    val total: Int,
    val total_pages: Int,
    val results: List<Collection>
)
