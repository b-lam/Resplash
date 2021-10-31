package com.b_lam.resplash.util

import com.b_lam.resplash.data.photo.model.Photo
import java.util.*

private const val RAW = "raw"
private const val FULL = "full"
private const val REGULAR = "regular"
private const val SMALL = "small"
private const val THUMB = "thumb"

fun getPhotoUrl(photo: Photo, quality: String?): String {
    return when (quality) {
        RAW -> photo.urls.raw
        FULL -> photo.urls.full
        REGULAR -> photo.urls.regular
        SMALL -> photo.urls.small
        THUMB -> photo.urls.thumb
        else -> photo.urls.regular
    }
}

val Photo.fileName: String
    get() = "${this.user?.name?.lowercase(Locale.ROOT)?.replace(" ", "-")}-${this.id}.jpg"
