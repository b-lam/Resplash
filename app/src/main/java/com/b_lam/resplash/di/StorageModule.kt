package com.b_lam.resplash.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.b_lam.resplash.domain.login.AccessTokenProvider
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.data.autowallpaper.AutoWallpaperDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {

    single(createdAtStart = true) {
        SharedPreferencesRepository(
            androidContext()
        )
    }
    single(createdAtStart = true) { AccessTokenProvider(androidContext()) }

    single { createWallpaperDatabase(androidApplication()) }
    single { get<AutoWallpaperDatabase>().autoWallpaperHistoryDao() }
    single { get<AutoWallpaperDatabase>().autoWallpaperCollectionDao() }
}

/**
 * Migration changes:
 *  - Upgrade `auto_wallpaper_collections` table due to change in `id` type (INTEGER -> TEXT).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Initiate db transaction.
        database.execSQL("BEGIN TRANSACTION;")

        // Create new auto_wallpaper_collections_upgrade table with updated schema.
        database.execSQL("""
            CREATE TABLE `auto_wallpaper_collections_upgrade` (
                `id` TEXT NOT NULL,
                `title` TEXT,
                `user_name` TEXT,
                `cover_photo` TEXT,
                `date_added` INTEGER,
                PRIMARY KEY(`id`)
            );
        """.trimIndent())

        // Copy old auto_wallpaper_collections data into the newly created table.
        database.execSQL("""
            INSERT INTO `auto_wallpaper_collections_upgrade` (
                id,
                title,
                user_name,
                cover_photo,
                date_added
            )
            SELECT
                CAST (id AS TEXT),
                title,
                user_name,
                cover_photo,
                date_added
            FROM `auto_wallpaper_collections`;
        """.trimIndent())

        // Remove old auto_wallpaper_collections table.
        database.execSQL("DROP TABLE auto_wallpaper_collections;")

        // Rename `auto_wallpaper_collections_upgrade` -> `auto_wallpaper_collections`.
        database.execSQL("""
            ALTER TABLE 'auto_wallpaper_collections_upgrade' RENAME TO 'auto_wallpaper_collections';
        """.trimIndent())

        // Create indices for `auto_wallpaper_collections`.
        database.execSQL("""
            CREATE INDEX `index_auto_wallpaper_collections_date_added`
            ON `auto_wallpaper_collections` (
                `date_added`
            );
        """.trimIndent())

        // Commit changes to db.
        database.execSQL("COMMIT;")
    }
}

private fun createWallpaperDatabase(application: Application) =
    Room.databaseBuilder(application, AutoWallpaperDatabase::class.java, "auto_wallpaper_db")
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
