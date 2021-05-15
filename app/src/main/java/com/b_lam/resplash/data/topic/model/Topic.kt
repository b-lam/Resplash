package com.b_lam.resplash.data.topic.model

import android.os.Parcelable
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Topic(
    val id: String,
    val slug: String,
    val title: String,
    val description: String?,
    val published_at: String?,
    val updated_at: String?,
    val starts_at: String?,
    val ends_at: String?,
    val featured: Boolean?,
    val total_photos: Int,
    val links: Links?,
    val status: String?,
    val owners: List<User>?,
    val top_contributors: List<User>?,
    val cover_photo: Photo?,
    val preview_photos: List<Photo>?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Links(
    val self: String,
    val html: String,
    val photos: String
) : Parcelable