package com.b_lam.resplash.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [(Wallpaper::class)], version = 1, exportSchema = false)
abstract class WallpaperDatabase : RoomDatabase() {

    companion object {
        private var INSTANCE: WallpaperDatabase? = null
        fun getDatabase(context: Context): WallpaperDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                        WallpaperDatabase::class.java, "wallpaper_database")
                        .allowMainThreadQueries().build()
            }
            return INSTANCE as WallpaperDatabase
        }
    }

    abstract fun wallpaperDao(): WallpaperDao
}