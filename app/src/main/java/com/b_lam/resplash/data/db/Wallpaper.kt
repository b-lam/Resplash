package com.b_lam.resplash.data.db

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_table")
data class Wallpaper(

        @PrimaryKey
        @NonNull
        @ColumnInfo(name = "id")
        var id: String = "",

        @ColumnInfo(name = "user_name")
        var userName: String = "",

        @ColumnInfo(name = "thumbnail")
        var thumbnail: String = "",

        @ColumnInfo(name = "date")
        var date: Long = 0
)