package com.b_lam.resplash.data.collection.model

import android.os.Parcelable
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.photo.model.Tag
import com.b_lam.resplash.data.user.model.User
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Collection(
    val id: Int,
    val title: String,
    val description: String?,
    val published_at: String?,
    val updated_at: String?,
    val curated: Boolean?,
    val featured: Boolean?,
    val total_photos: Int,
    val private: Boolean?,
    val share_key: String?,
    val tags: List<Tag>?,
    val cover_photo: Photo?,
    val preview_photos: List<Photo>?,
    val user: User?,
    val links: Links?
) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class Links(
    val self: String,
    val html: String,
    val photos: String
) : Parcelable
