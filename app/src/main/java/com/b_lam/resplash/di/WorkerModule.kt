package com.b_lam.resplash.di

import com.b_lam.resplash.worker.AutoWallpaperWorker
import com.b_lam.resplash.worker.DownloadWorker
import com.b_lam.resplash.worker.MuzeiWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {

    worker { AutoWallpaperWorker(get(), get(), get(), get(), get(), get()) }
    worker { AutoWallpaperWorker.FutureAutoWallpaperWorker(get(), get(), get()) }
    worker { DownloadWorker(get(), get(), get(), get()) }
    worker { MuzeiWorker(get(), get(), get(), get()) }
}