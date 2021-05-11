package com.b_lam.resplash.data.autowallpaper

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperHistory

@Database(
    entities = [
        AutoWallpaperHistory::class,
        AutoWallpaperCollection::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AutoWallpaperDatabase : RoomDatabase() {

    abstract fun autoWallpaperHistoryDao(): AutoWallpaperHistoryDao
    abstract fun autoWallpaperCollectionDao(): AutoWallpaperCollectionDao

    companion object {

        /**
         * Migrate from:
         * version 1 - using Room where the {@link AutoWallpaperCollection#id} is an Int
         * to
         * version 2 - using Room where the {@link AutoWallpaperCollection#id} is a String
         *
         * SQLite supports a limited operations for ALTER. Changing the type of a column is not
         * directly supported, so this is what we need to do:
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE auto_wallpaper_collections_new (
                        id TEXT NOT NULL, 
                        title TEXT, 
                        user_name TEXT, 
                        cover_photo TEXT,
                        date_added INTEGER, 
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                database.execSQL("""
                    INSERT INTO auto_wallpaper_collections_new (
                        id, title, user_name, cover_photo, date_added
                    )
                    SELECT id, title, user_name, cover_photo, date_added 
                    FROM auto_wallpaper_collections
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE auto_wallpaper_collections")
                database.execSQL("""
                    ALTER TABLE auto_wallpaper_collections_new
                    RENAME TO auto_wallpaper_collections
                    """.trimIndent()
                )
                database.execSQL("""
                    CREATE INDEX index_auto_wallpaper_collections_date_added 
                    ON auto_wallpaper_collections (date_added)
                    """.trimIndent()
                )
            }
        }
    }
}