package com.b_lam.resplash.di

import android.app.Application
import androidx.room.Room
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

private fun createWallpaperDatabase(application: Application) =
    Room.databaseBuilder(application, AutoWallpaperDatabase::class.java, "auto_wallpaper_db")
        .fallbackToDestructiveMigration()
        .build()
