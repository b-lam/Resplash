package com.b_lam.resplash.data.common.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PhotoStatistics(
    val id: String,
    val downloads: Downloads,
    val views: Views,
    val likes: Likes
) : Parcelable

@JsonClass(generateAdapter = true)
data class UserStatistics(
    val username: String,
    val downloads: Downloads,
    val views: Views,
    val likes: Likes
)

@Parcelize
@JsonClass(generateAdapter = true)
data class Downloads(
    val total: Int,
    val historical: Historical
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Views(
    val total: Int,
    val historical: Historical
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Likes(
    val total: Int,
    val historical: Historical
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Historical(
    val change: Int,
    val resolution: String,
    val quality: String,
    val values: List<Value>
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Value(
    val date: String,
    val value: Int
) : Parcelable
