package com.b_lam.resplash.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.b_lam.resplash.data.db.Wallpaper
import com.b_lam.resplash.data.repository.WallpaperRepository

class WallpaperListViewModel(application: Application) : AndroidViewModel(application) {

    private var mWallpaperRepository: WallpaperRepository = WallpaperRepository(getApplication())

    private var mAllWallpapers: LiveData<List<Wallpaper>>

    init {
        mAllWallpapers = mWallpaperRepository.getAllWallpapers()
    }

    fun getAllWallpapers(): LiveData<List<Wallpaper>> {
        return mAllWallpapers
    }

    fun deleteAllWallpapers() {
        mWallpaperRepository.deleteAllWallpapers()
    }

    fun deleteOldWallpapers() {
        mWallpaperRepository.deleteOldWallpapers()
    }
}