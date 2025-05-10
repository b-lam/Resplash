package com.b_lam.resplash.data.autowallpaper.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "auto_wallpaper_collections",
    indices = [Index(value = ["date_added"])]
)
data class AutoWallpaperCollection(

    @PrimaryKey @JvmField val id: String = "",
    val title: String? = null,
    val user_name: String? = null,
    val cover_photo: String? = null,
    var date_added: Long? = null
)