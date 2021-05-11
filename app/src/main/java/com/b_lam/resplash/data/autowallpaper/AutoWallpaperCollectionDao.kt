package com.b_lam.resplash.data.autowallpaper

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection

@Dao
interface AutoWallpaperCollectionDao {

    @Query("SELECT * FROM auto_wallpaper_collections ORDER BY date_added DESC")
    fun getSelectedAutoWallpaperCollections(): LiveData<List<AutoWallpaperCollection>>

    @Query("SELECT id FROM auto_wallpaper_collections")
    fun getSelectedAutoWallpaperCollectionIds(): LiveData<List<String>>

    @Query("SELECT id FROM auto_wallpaper_collections LIMIT 1 OFFSET :offset")
    suspend fun getRandomAutoWallpaperCollectionId(offset: Int): String?

    @Query("SELECT COUNT(*) FROM auto_wallpaper_collections")
    suspend fun getNumberOfAutoWallpaperCollections(): Int

    @Query("SELECT COUNT(*) FROM auto_wallpaper_collections")
    fun getNumberOfAutoWallpaperCollectionsLiveData(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM auto_wallpaper_collections WHERE id = :id")
    fun getCountById(id: String): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: AutoWallpaperCollection)

    @Query("DELETE FROM auto_wallpaper_collections WHERE id = :id")
    suspend fun delete(id: String)
}