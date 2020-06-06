package com.b_lam.resplash.data.autowallpaper

import androidx.paging.DataSource
import androidx.room.*
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperHistory
import java.util.concurrent.TimeUnit

@Dao
interface AutoWallpaperHistoryDao {

    @Query("SELECT * FROM auto_wallpaper_history ORDER BY date DESC")
    fun getAllAutoWallpaperHistory(): DataSource.Factory<Int, AutoWallpaperHistory>

    @Query("DELETE FROM auto_wallpaper_history")
    suspend fun deleteAllAutoWallpaperHistory()

    @Query("DELETE FROM auto_wallpaper_history WHERE :now - date > :threshold")
    suspend fun deleteOldAutoWallpaperHistory(now: Long = System.currentTimeMillis(), threshold: Long = ONE_MONTH)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallpaper: AutoWallpaperHistory)

    @Update
    suspend fun update(wallpaper: AutoWallpaperHistory)

    @Delete
    suspend fun delete(wallpaper: AutoWallpaperHistory)

    companion object {

        private val ONE_MONTH = TimeUnit.DAYS.toMillis(30)
    }
}