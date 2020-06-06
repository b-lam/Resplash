package com.b_lam.resplash.data.authorization.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccessToken(
    val access_token: String,
    val token_type: String?,
    val scope: String?,
    val create_at: Int?
)
