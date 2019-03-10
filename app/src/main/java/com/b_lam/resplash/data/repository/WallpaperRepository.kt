package com.b_lam.resplash.data.repository

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.b_lam.resplash.data.db.Wallpaper
import com.b_lam.resplash.data.db.WallpaperDatabase
import java.util.concurrent.TimeUnit

class WallpaperRepository(context: Context) {

    private val ONE_WEEK = TimeUnit.DAYS.toMillis(7)

    private val mDatabase: WallpaperDatabase = WallpaperDatabase.getDatabase(context)
    private var mAllWallpapers: LiveData<List<Wallpaper>>

    init {
        mAllWallpapers = mDatabase.wallpaperDao().getAllWallpapers()
    }

    fun getAllWallpapers(): LiveData<List<Wallpaper>> {
        return mAllWallpapers
    }

    fun deleteAllWallpapers() {
        mDatabase.wallpaperDao().deleteAllWallpapers()
    }

    fun deleteOldWallpapers() {
        mDatabase.wallpaperDao().deleteOldWallpapers(System.currentTimeMillis(), ONE_WEEK)
    }

    fun addWallpaper(wallpaper: Wallpaper) {
        AddWallpaperAsyncTask(mDatabase).execute(wallpaper)
    }

    class AddWallpaperAsyncTask(database: WallpaperDatabase): AsyncTask<Wallpaper, Void, Void>() {
        private var mDatabase = database

        override fun doInBackground(vararg params: Wallpaper): Void? {
            mDatabase.wallpaperDao().addWallpaper(params[0])
            return null
        }
    }
}