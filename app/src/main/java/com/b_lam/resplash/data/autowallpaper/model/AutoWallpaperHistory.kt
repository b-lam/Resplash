package com.b_lam.resplash.data.autowallpaper.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "auto_wallpaper_history",
    indices = [Index(value = ["date"])]
)
data class AutoWallpaperHistory(
    val photo_id: String?,
    val username: String?,
    val name: String?,
    val profile_picture: String?,
    val thumbnail_url: String?,
    val width: Int?,
    val height: Int?,
    val color: String?,
    val date: Long?
) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}