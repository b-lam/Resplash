package com.b_lam.resplash.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.b_lam.resplash.data.autowallpaper.AutoWallpaperDatabase
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AutoWallpaperDatabaseMigrationTest {

    private val testAutoWallpaperCollection =
        AutoWallpaperCollection("123", "Title", "Brandon", "Some URL", 123)

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AutoWallpaperDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateFrom1To2() {
        helper.createDatabase(TEST_DB, 1).apply {
            insertAutoWallpaperCollection(
                123,
                testAutoWallpaperCollection.title,
                testAutoWallpaperCollection.user_name,
                testAutoWallpaperCollection.cover_photo,
                testAutoWallpaperCollection.date_added,
                this
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            AutoWallpaperDatabase.MIGRATION_1_2
        )

        val dbAutoWallpaperCollection =
            getMigratedDatabase().autoWallpaperCollectionDao().getAutoWallpaperCollection("123")
        assertEquals(dbAutoWallpaperCollection.id, "123")
        assertEquals(dbAutoWallpaperCollection.title, testAutoWallpaperCollection.title)
        assertEquals(dbAutoWallpaperCollection.user_name, testAutoWallpaperCollection.user_name)
        assertEquals(dbAutoWallpaperCollection.cover_photo, testAutoWallpaperCollection.cover_photo)
        assertEquals(dbAutoWallpaperCollection.date_added, testAutoWallpaperCollection.date_added)
    }

    private fun getMigratedDatabase() = Room.databaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        AutoWallpaperDatabase::class.java,
        TEST_DB
    ).addMigrations(*ALL_MIGRATIONS).build().apply {
        helper.closeWhenFinished(this)
    }

    private fun insertAutoWallpaperCollection(
        id: Int,
        title: String?,
        userName: String?,
        coverPhoto: String?,
        dateAdded: Long?,
        db: SupportSQLiteDatabase
    ) {
        val values = ContentValues().apply {
            put("id", id)
            put("title", title)
            put("user_name", userName)
            put("cover_photo", coverPhoto)
            put("date_added", dateAdded)
        }
        db.insert("auto_wallpaper_collections", SQLiteDatabase.CONFLICT_REPLACE, values)
    }

    companion object {
        private const val TEST_DB = "migration-test"

        private val ALL_MIGRATIONS = arrayOf(AutoWallpaperDatabase.MIGRATION_1_2)
    }
}