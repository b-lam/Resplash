package com.b_lam.resplash.di

import androidx.work.WorkerParameters
import com.b_lam.resplash.worker.AutoWallpaperWorker
import com.b_lam.resplash.worker.DownloadWorker
import com.b_lam.resplash.worker.FutureAutoWallpaperWorker
import com.b_lam.resplash.worker.MuzeiWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {

    worker { (workerParams: WorkerParameters) ->
        AutoWallpaperWorker(
            context = get(),
            params = workerParams,
            photoRepository = get(),
            autoWallpaperRepository = get(),
            downloadService = get(),
            notificationManager = get()
        )
    }
    worker { (workerParams: WorkerParameters) ->
        FutureAutoWallpaperWorker(
            context = get(),
            params = workerParams,
            sharedPreferencesRepository = get(),
            notificationManager = get()
        )
    }
    worker { (workerParams: WorkerParameters) ->
        DownloadWorker(
            context = get(),
            params = workerParams,
            downloadService = get(),
            notificationManager = get()
        )
    }
    worker { (workerParams: WorkerParameters) ->
        MuzeiWorker(
            context = get(),
            params = workerParams,
            photoRepository = get(),
            autoWallpaperRepository = get()
        )
    }
}