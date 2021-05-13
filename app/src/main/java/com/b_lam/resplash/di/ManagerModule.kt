package com.b_lam.resplash.di

import android.app.WallpaperManager
import com.b_lam.resplash.util.NotificationManager
import com.b_lam.resplash.util.download.DownloadManagerWrapper
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val managerModule = module {

    single(createdAtStart = true) { NotificationManager(androidContext()) }
    single { DownloadManagerWrapper(androidContext()) }
    single { WallpaperManager.getInstance(androidApplication()) }
}