package com.b_lam.resplash.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WallpaperDao {

    @Query("select * from wallpaper_table")
    fun getAllWallpapers(): LiveData<List<Wallpaper>>

    @Query("delete from wallpaper_table")
    fun deleteAllWallpapers()

    @Query("delete from wallpaper_table where :now - date > :threshold")
    fun deleteOldWallpapers(now: Long, threshold: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addWallpaper(wallpaper: Wallpaper)

    @Update
    fun updateWallpaper(wallpaper: Wallpaper)

    @Delete
    fun deleteWallpaper(wallpaper: Wallpaper)
}