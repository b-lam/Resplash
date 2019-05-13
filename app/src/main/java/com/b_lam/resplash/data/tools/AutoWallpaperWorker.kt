package com.b_lam.resplash.data.tools

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.work.*
import androidx.work.impl.utils.futures.SettableFuture
import com.b_lam.resplash.R
import com.b_lam.resplash.data.db.Wallpaper
import com.b_lam.resplash.data.model.Photo
import com.b_lam.resplash.data.repository.WallpaperRepository
import com.b_lam.resplash.data.service.PhotoService
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.TimeUnit

@SuppressLint("RestrictedApi")
class AutoWallpaperWorker(context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {

    private var photoService: PhotoService? = null

    override fun onStopped() {
        super.onStopped()
        photoService?.cancel()
    }

    override fun startWork(): ListenableFuture<Result> {
        val future = SettableFuture.create<Result>()
        photoService = PhotoService.getService()

        val featured = inputData.getBoolean(AUTO_WALLPAPER_CATEGORY_FEATURED_KEY, false)
        val customCategory = inputData.getString(AUTO_WALLPAPER_CATEGORY_CUSTOM_KEY)

        val onRequestPhotosListener = object : PhotoService.OnRequestPhotosListener {
            override fun onRequestPhotosSuccess(call: Call<List<Photo>>, response: Response<List<Photo>>) {
                if (response.isSuccessful) {
                    val photos = response.body()
                    if (photos != null && photos.isNotEmpty()) {
                        val photo = photos[0]
                        downloadAndSetWallpaper(photo, photoService, future)
                    }
                } else {
                    photoService?.requestRandomPhotos(null, null, null, null, null, 1, object : PhotoService.OnRequestPhotosListener {
                        override fun onRequestPhotosSuccess(call: Call<List<Photo>>, response: Response<List<Photo>>) {
                            if (response.isSuccessful) {
                                val photos = response.body()
                                if (photos != null && photos.isNotEmpty()) {
                                    val photo = photos[0]
                                    downloadAndSetWallpaper(photo, photoService, future)
                                }
                            }
                        }

                        override fun onRequestPhotosFailed(call: Call<List<Photo>>, t: Throwable) {
                            future.set(Result.retry())
                        }
                    })
                }
            }

            override fun onRequestPhotosFailed(call: Call<List<Photo>>, t: Throwable) {
                future.set(Result.retry())
            }
        }

        photoService?.requestRandomPhotos(null, featured, null, customCategory, null, 1, onRequestPhotosListener)

        return future
    }

    private fun downloadAndSetWallpaper(photo: Photo, photoService: PhotoService?, future: SettableFuture<Result>) {
        Thread {
            val request = Request.Builder()
                    .url(getUrlFromQuality(photo, inputData.getString(AUTO_WALLPAPER_QUALITY_KEY)))
                    .build()

            OkHttpClient().newCall(request).execute().use { response ->
                val inputStream = response.body()?.byteStream()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val screenSelect = inputData.getInt(AUTO_WALLPAPER_SELECT_SCREEN_KEY,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    WallpaperManager.getInstance(applicationContext).setStream(inputStream, null,
                            true, screenSelect)
                } else {
                    WallpaperManager.getInstance(applicationContext).setStream(inputStream)
                }

                photoService?.reportDownload(photo.id, null)

                addWallpaperToHistory(photo)

                future.set(Result.success())

                response.close()
            }
        }.start()
    }

    private fun addWallpaperToHistory(photo: Photo) {
        val repository = WallpaperRepository(applicationContext)

        val thumbnailUrl = getUrlFromQuality(photo,
                inputData.getString(AUTO_WALLPAPER_THUMBNAIL_KEY))

        repository.addWallpaper(Wallpaper(photo.id, photo.user.name, thumbnailUrl, System.currentTimeMillis()))
    }

    private fun getUrlFromQuality(photo: Photo, quality: String?): String {
        return when (quality) {
            "Raw" -> photo.urls.raw
            "Full" -> photo.urls.full
            "Regular" -> photo.urls.regular
            "Small" -> photo.urls.small
            "Thumb" -> photo.urls.thumb
            else -> photo.urls.regular
        }
    }

    companion object {

        private const val AUTO_WALLPAPER_JOB_ID = "auto_wallpaper_job"
        const val AUTO_WALLPAPER_SINGLE_JOB_ID = "auto_wallpaper_single_job"

        const val AUTO_WALLPAPER_CATEGORY_FEATURED_KEY = "auto_wallpaper_category_featured"
        const val AUTO_WALLPAPER_CATEGORY_CUSTOM_KEY = "auto_wallpaper_category_custom"
        const val AUTO_WALLPAPER_QUALITY_KEY = "auto_wallpaper_quality"
        const val AUTO_WALLPAPER_THUMBNAIL_KEY = "auto_wallpaper_thumbnail"
        const val AUTO_WALLPAPER_SELECT_SCREEN_KEY = "auto_wallpaper_select_screen"

        //Schedule wallpaper to change now regardless of conditions
        fun scheduleAutoWallpaperJobSingle(context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false)

            if (autoWallpaperEnabled) {
                val changeWallpaperInterval = sharedPreferences.getString("auto_wallpaper_interval",
                        context.getString(R.string.auto_wallpaper_interval_default))
                val changeWallpaperIntervalMinutes = changeWallpaperInterval!!.toLong()

                val request = OneTimeWorkRequestBuilder<AutoWallpaperWorker>()
                        .setInputData(getAutoWallpaperParams(context, sharedPreferences))
                        .build()

                val requestFuture = OneTimeWorkRequestBuilder<FutureAutoWallpaperWorker>()
                        .setInitialDelay(changeWallpaperIntervalMinutes, TimeUnit.MINUTES)
                        .build()

                WorkManager.getInstance().enqueueUniqueWork(AUTO_WALLPAPER_SINGLE_JOB_ID, ExistingWorkPolicy.REPLACE, request)
                WorkManager.getInstance().enqueueUniqueWork(AUTO_WALLPAPER_JOB_ID, ExistingWorkPolicy.REPLACE, requestFuture)
            } else {
                WorkManager.getInstance().cancelUniqueWork(AUTO_WALLPAPER_SINGLE_JOB_ID)
                WorkManager.getInstance().cancelUniqueWork(AUTO_WALLPAPER_JOB_ID)
            }
        }

        //Schedule wallpaper to change with configured conditions
        fun scheduleAutoWallpaperJob(context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false)

            if (autoWallpaperEnabled) {
                val deviceOnWifiCondition = sharedPreferences.getBoolean("auto_wallpaper_on_wifi", true)
                val deviceChargingCondition = sharedPreferences.getBoolean("auto_wallpaper_charging", true)
                val deviceIdleCondition = sharedPreferences.getBoolean("auto_wallpaper_idle", true)
                val changeWallpaperInterval = sharedPreferences.getString("auto_wallpaper_interval",
                        context.getString(R.string.auto_wallpaper_interval_default))
                val changeWallpaperIntervalMinutes = changeWallpaperInterval!!.toLong()

                val constraints = Constraints.Builder()
                            .setRequiredNetworkType( if (deviceOnWifiCondition) NetworkType.UNMETERED else NetworkType.NOT_REQUIRED )
                            .setRequiresCharging(deviceChargingCondition)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    constraints.setRequiresDeviceIdle(deviceIdleCondition)
                }

                val request = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(changeWallpaperIntervalMinutes, TimeUnit.MINUTES)
                        .setInputData(getAutoWallpaperParams(context, sharedPreferences))
                        .setConstraints(constraints.build())
                        .build()

                WorkManager.getInstance().enqueueUniquePeriodicWork(AUTO_WALLPAPER_JOB_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
            } else {
                WorkManager.getInstance().cancelUniqueWork(AUTO_WALLPAPER_JOB_ID)
            }
        }

        private fun getAutoWallpaperParams(context: Context, sharedPreferences: SharedPreferences): Data {
            val builder = Data.Builder()

            val category = sharedPreferences.getString("auto_wallpaper_category",
                    context.getString(R.string.auto_wallpaper_category_default))

            when (category) {
                "Featured" -> builder.putBoolean(AUTO_WALLPAPER_CATEGORY_FEATURED_KEY, true)
                "Custom" -> builder.putString(AUTO_WALLPAPER_CATEGORY_CUSTOM_KEY,
                        sharedPreferences.getString("auto_wallpaper_custom_category",
                                context.getString(R.string.auto_wallpaper_custom_category_default)))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val screenSelect = sharedPreferences.getString("auto_wallpaper_select_screen",
                        context.getString(R.string.auto_wallpaper_select_screen_default))
                when (screenSelect) {
                    "Home screen" -> builder.putInt(AUTO_WALLPAPER_SELECT_SCREEN_KEY, WallpaperManager.FLAG_SYSTEM)
                    "Lock screen" -> builder.putInt(AUTO_WALLPAPER_SELECT_SCREEN_KEY, WallpaperManager.FLAG_LOCK)
                    "Both" -> builder.putInt(AUTO_WALLPAPER_SELECT_SCREEN_KEY, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                }
            }

            builder.putString(AUTO_WALLPAPER_QUALITY_KEY,
                    sharedPreferences.getString("wallpaper_quality", "Full"))

            builder.putString(AUTO_WALLPAPER_THUMBNAIL_KEY,
                    sharedPreferences.getString("load_quality", "Regular"))

            return builder.build()
        }

        class FutureAutoWallpaperWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
            override fun doWork(): Result {
                scheduleAutoWallpaperJob(applicationContext)
                return Result.success()
            }
        }
    }
}