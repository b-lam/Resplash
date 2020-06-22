package com.b_lam.resplash.worker

import android.app.WallpaperManager
import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Rect
import android.os.Build
import androidx.work.*
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperHistory
import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.domain.autowallpaper.AutoWallpaperRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.util.Result.Error
import com.b_lam.resplash.util.Result.Success
import com.b_lam.resplash.util.getPhotoUrl
import com.b_lam.resplash.util.safeApiCall
import com.b_lam.resplash.util.screenHeight
import com.b_lam.resplash.util.screenWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class AutoWallpaperWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val photoRepository: PhotoRepository by inject()
    private val autoWallpaperRepository: AutoWallpaperRepository by inject()
    private val downloadService: DownloadService by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (inputData.getBoolean(KEY_AUTO_WALLPAPER_PORTRAIT_MODE_ONLY, false)) {
            val screenOrientation = appContext.resources.configuration.orientation
            if (screenOrientation == ORIENTATION_LANDSCAPE) {
                return@withContext Result.retry()
            }
        }

        val orientation = inputData.getString(KEY_AUTO_WALLPAPER_ORIENTATION)
        val contentFilter = inputData.getString(KEY_AUTO_WALLPAPER_CONTENT_FILTER)

        try {
            val result = when (inputData.getString(KEY_AUTO_WALLPAPER_SOURCE)) {
                Source.FEATURED -> photoRepository.getRandomPhoto(
                    featured = true,
                    orientation = orientation,
                    contentFilter = contentFilter
                )
                Source.COLLECTIONS -> {
                    val collectionId = getCollectionId()
                    photoRepository.getRandomPhoto(
                        collectionId = collectionId,
                        orientation = orientation,
                        contentFilter = contentFilter
                    )
                }
                Source.USER -> {
                    val username = inputData.getString(KEY_AUTO_WALLPAPER_USERNAME)
                        ?.replace("@", "")
                    photoRepository.getRandomPhoto(
                        username = username,
                        orientation = orientation,
                        contentFilter = contentFilter
                    )
                }
                Source.SEARCH -> {
                    val query = inputData.getString(KEY_AUTO_WALLPAPER_SEARCH_TERMS)
                        ?.split(",")?.random()?.trim()
                    photoRepository.getRandomPhoto(
                        query = query,
                        orientation = orientation,
                        contentFilter = contentFilter
                    )
                }
                else -> photoRepository.getRandomPhoto(
                    orientation = orientation,
                    contentFilter = contentFilter
                )
            }

            if (result is Success) {
                if (downloadAndSetWallpaper(result.value)) {
                    trackDownload(result.value.id)
                    addWallpaperToHistory(result.value)
                    return@withContext Result.success()
                } else {
                    return@withContext Result.retry()
                }
            } else if (result is Error && result.code == 404) {
                return@withContext Result.failure()
            } else {
                return@withContext Result.retry()
            }
        } catch (e: Throwable) {
            return@withContext Result.failure()
        }
    }

    private suspend fun getCollectionId(): Int? {
        return autoWallpaperRepository.getRandomAutoWallpaperCollectionId()
    }

    private suspend fun downloadAndSetWallpaper(photo: Photo): Boolean {
        val url = getPhotoUrl(photo, inputData.getString(KEY_AUTO_WALLPAPER_QUALITY))

        try {
            downloadService.downloadFile(url).byteStream().use {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val centerCropRect =
                        if (inputData.getBoolean(KEY_AUTO_WALLPAPER_CENTER_CROP, true)) {
                            if (photo.width != null && photo.height != null) {
                                getCenterCropRect(
                                    min(screenWidth, screenHeight).toDouble(),
                                    max(screenWidth, screenHeight).toDouble(),
                                    photo.width.toDouble(),
                                    photo.height.toDouble()
                                )
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    val screenSelect = inputData.getInt(KEY_AUTO_WALLPAPER_SELECT_SCREEN,
                        WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    WallpaperManager.getInstance(appContext).setStream(it, centerCropRect,
                        true, screenSelect)
                } else {
                    WallpaperManager.getInstance(appContext).setStream(it)
                }
                return true
            }
        } catch (e: Throwable) {
            return false
        }
    }

    private fun getCenterCropRect(
        screenWidth: Double,
        screenHeight: Double,
        photoWidth: Double,
        photoHeight: Double
    ): Rect? {
        if (screenWidth > 0 && screenHeight > 0 && photoWidth > 0 && photoHeight > 0) {
            val screenAspectRatio = screenWidth / screenHeight
            val photoAspectRatio = photoWidth / photoHeight
            val resizeFactor = if (screenAspectRatio >= photoAspectRatio) {
                photoWidth / screenWidth
            } else {
                photoHeight / screenHeight
            }
            val newWidth = screenWidth * resizeFactor
            val newHeight = screenHeight * resizeFactor
            val newLeft = (photoWidth - newWidth) / 2
            val newTop = (photoHeight - newHeight) / 2
            val rect = Rect(newLeft.toInt(), newTop.toInt(),
                (newWidth + newLeft).toInt(), (newHeight + newTop).toInt())
            return if (rect.isValid()) rect else null
        }
        return null
    }

    private fun Rect.isValid(): Boolean {
        return right >= 0 && left in 0..right && bottom >= 0 && top in 0..bottom
    }

    private suspend fun trackDownload(id: String) = safeApiCall(Dispatchers.IO) {
        downloadService.trackDownload(id)
    }

    private suspend fun addWallpaperToHistory(photo: Photo) {
        val url = getPhotoUrl(photo, inputData.getString(KEY_AUTO_WALLPAPER_THUMBNAIL_QUALITY))
        autoWallpaperRepository.addToAutoWallpaperHistory(
            AutoWallpaperHistory(
                photo.id,
                photo.user?.username ?: "",
                photo.user?.name ?: "",
                photo.user?.profile_image?.large ?: "",
                url,
                photo.width ?: 0,
                photo.height ?: 0,
                photo.color,
                System.currentTimeMillis()
            )
        )
    }

    companion object {

        private const val AUTO_WALLPAPER_JOB_ID = "auto_wallpaper_job_id"
        const val AUTO_WALLPAPER_SINGLE_JOB_ID = "auto_wallpaper_single_job_id"

        const val KEY_AUTO_WALLPAPER_QUALITY = "key_auto_wallpaper_quality"
        const val KEY_AUTO_WALLPAPER_THUMBNAIL_QUALITY = "key_auto_wallpaper_thumbnail_quality"
        const val KEY_AUTO_WALLPAPER_SOURCE = "key_auto_wallpaper_source"
        const val KEY_AUTO_WALLPAPER_USERNAME = "key_auto_wallpaper_username"
        const val KEY_AUTO_WALLPAPER_SEARCH_TERMS = "key_auto_wallpaper_search_terms"
        const val KEY_AUTO_WALLPAPER_CENTER_CROP = "key_auto_wallpaper_center_crop"
        const val KEY_AUTO_WALLPAPER_PORTRAIT_MODE_ONLY = "key_auto_wallpaper_portrait_mode_only"
        const val KEY_AUTO_WALLPAPER_SELECT_SCREEN = "key_auto_wallpaper_select_screen"
        const val KEY_AUTO_WALLPAPER_ORIENTATION = "key_auto_wallpaper_orientation"
        const val KEY_AUTO_WALLPAPER_CONTENT_FILTER = "key_auto_wallpaper_content_filter"

        object Source {

            const val ALL = "all"
            const val FEATURED = "featured"
            const val COLLECTIONS = "collections"
            const val USER = "user"
            const val SEARCH = "search"

            val SOURCE_UNENTITLED = listOf(ALL, FEATURED)
            val SOURCE_ENTITLED = listOf(COLLECTIONS, USER, SEARCH)
        }

        //Schedule wallpaper to change now regardless of conditions and schedule future change
        fun scheduleSingleAutoWallpaperJob(
            context: Context,
            sharedPreferencesRepository: SharedPreferencesRepository
        ) {
            with(sharedPreferencesRepository) {
                if (autoWallpaperEnabled) {
                    val data = getAutoWallpaperWorkData(sharedPreferencesRepository)
                    val request = OneTimeWorkRequestBuilder<AutoWallpaperWorker>()
                        .setInputData(data)
                        .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        )
                        .build()
                    val requestFuture = OneTimeWorkRequestBuilder<FutureAutoWallpaperWorker>()
                        .setInitialDelay(autoWallpaperInterval, TimeUnit.MINUTES)
                        .build()

                    WorkManager.getInstance(context).enqueueUniqueWork(
                        AUTO_WALLPAPER_SINGLE_JOB_ID,
                        ExistingWorkPolicy.REPLACE,
                        request
                    )
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        AUTO_WALLPAPER_JOB_ID,
                        ExistingWorkPolicy.REPLACE,
                        requestFuture
                    )
                } else {
                    WorkManager.getInstance(context).cancelUniqueWork(AUTO_WALLPAPER_SINGLE_JOB_ID)
                    WorkManager.getInstance(context).cancelUniqueWork(AUTO_WALLPAPER_JOB_ID)
                }
            }
        }

        //Schedule wallpaper to change with configured conditions
        fun scheduleAutoWallpaperJob(
            context: Context,
            sharedPreferencesRepository: SharedPreferencesRepository
        ) {
            with(sharedPreferencesRepository) {
                if (autoWallpaperEnabled) {
                    val data = getAutoWallpaperWorkData(sharedPreferencesRepository)
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType( if (autoWallpaperOnWifi) NetworkType.UNMETERED else NetworkType.NOT_REQUIRED )
                        .setRequiresCharging(autoWallpaperCharging)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        constraints.setRequiresDeviceIdle(autoWallpaperIdle)
                    }

                    val request = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(autoWallpaperInterval, TimeUnit.MINUTES)
                        .setInputData(data)
                        .setConstraints(constraints.build())
                        .build()

                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        AUTO_WALLPAPER_JOB_ID,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        request
                    )
                } else {
                    WorkManager.getInstance(context).cancelUniqueWork(AUTO_WALLPAPER_JOB_ID)
                }
            }
        }

        private fun getAutoWallpaperWorkData(
            sharedPreferencesRepository: SharedPreferencesRepository
        ) = workDataOf(
            KEY_AUTO_WALLPAPER_QUALITY to sharedPreferencesRepository.wallpaperQuality,
            KEY_AUTO_WALLPAPER_THUMBNAIL_QUALITY to sharedPreferencesRepository.loadQuality,
            KEY_AUTO_WALLPAPER_SOURCE to sharedPreferencesRepository.autoWallpaperSource,
            KEY_AUTO_WALLPAPER_USERNAME to sharedPreferencesRepository.autoWallpaperUsername,
            KEY_AUTO_WALLPAPER_SEARCH_TERMS to sharedPreferencesRepository.autoWallpaperSearchTerms,
            KEY_AUTO_WALLPAPER_CENTER_CROP to sharedPreferencesRepository.autoWallpaperCenterCrop,
            KEY_AUTO_WALLPAPER_PORTRAIT_MODE_ONLY to sharedPreferencesRepository.autoWallpaperPortraitModeOnly,
            KEY_AUTO_WALLPAPER_SELECT_SCREEN to sharedPreferencesRepository.autoWallpaperSelectScreen,
            KEY_AUTO_WALLPAPER_ORIENTATION to sharedPreferencesRepository.autoWallpaperOrientation,
            KEY_AUTO_WALLPAPER_CONTENT_FILTER to sharedPreferencesRepository.autoWallpaperContentFilter
        )

        class FutureAutoWallpaperWorker(
            private val appContext: Context,
            params: WorkerParameters
        ) : Worker(appContext, params), KoinComponent {

            override fun doWork(): Result {
                scheduleAutoWallpaperJob(appContext, get())
                return Result.success()
            }
        }
    }
}