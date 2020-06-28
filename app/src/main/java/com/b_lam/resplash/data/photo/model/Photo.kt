package com.b_lam.resplash.data.photo.model

import android.os.Parcelable
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.common.model.PhotoStatistics
import com.b_lam.resplash.data.user.model.User
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Photo(
    val id: String,
    val created_at: String?,
    val updated_at: String?,
    val width: Int?,
    val height: Int?,
    val color: String? = "#E0E0E0",
    val views: Int?,
    val downloads: Int?,
    val likes: Int?,
    var liked_by_user: Boolean?,
    val description: String?,
    val alt_description: String?,
    val exif: Exif?,
    val location: Location?,
    val tags: List<Tag>?,
    val current_user_collections: List<Collection>?,
    val sponsorship: Sponsorship?,
    val urls: Urls,
    val links: Links?,
    val user: User?,
    val statistics: PhotoStatistics?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Exif(
    val make: String?,
    val model: String?,
    val exposure_time: String?,
    val aperture: String?,
    val focal_length: String?,
    val iso: Int?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Location(
    val city: String?,
    val country: String?,
    val position: Position?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Position(
    val latitude: Double?,
    val longitude: Double?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Tag(
    val type: String?,
    val title: String?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Urls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Links(
    val self: String,
    val html: String,
    val download: String,
    val download_location: String
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Sponsorship(
    val sponsor: User?
) : Parcelable