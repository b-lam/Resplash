package com.b_lam.resplash.data.collection.model

import android.os.Parcelable
import com.b_lam.resplash.data.photo.model.Photo
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CollectionPhotoResult(
    val photo: Photo?,
    val collection: Collection?
) : Parcelable