package com.b_lam.resplash.data.autowallpaper

import androidx.room.Database
import androidx.room.RoomDatabase
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperHistory

@Database(
    entities = [
        AutoWallpaperHistory::class,
        AutoWallpaperCollection::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AutoWallpaperDatabase : RoomDatabase() {

    abstract fun autoWallpaperHistoryDao(): AutoWallpaperHistoryDao
    abstract fun autoWallpaperCollectionDao(): AutoWallpaperCollectionDao
}