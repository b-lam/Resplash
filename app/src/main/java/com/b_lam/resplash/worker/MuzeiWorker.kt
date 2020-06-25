package com.b_lam.resplash.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.*
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.autowallpaper.AutoWallpaperRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.util.Result.Error
import com.b_lam.resplash.util.Result.Success
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class MuzeiWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val photoRepository: PhotoRepository by inject()
    private val autoWallpaperRepository: AutoWallpaperRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO){

        try {
            val result = when (inputData.getString(AutoWallpaperWorker.KEY_AUTO_WALLPAPER_SOURCE)) {
                AutoWallpaperWorker.Companion.Source.FEATURED -> {
                    photoRepository.getRandomPhoto(featured = true)
                }
                AutoWallpaperWorker.Companion.Source.COLLECTIONS -> {
                    val collectionId = autoWallpaperRepository.getRandomAutoWallpaperCollectionId()
                    photoRepository.getRandomPhoto(collectionId = collectionId)
                }
                AutoWallpaperWorker.Companion.Source.USER -> {
                    val username = inputData.getString(AutoWallpaperWorker.KEY_AUTO_WALLPAPER_USERNAME)
                        ?.replace("@", "")
                    photoRepository.getRandomPhoto(username = username)
                }
                AutoWallpaperWorker.Companion.Source.SEARCH -> {
                    val query = inputData.getString(AutoWallpaperWorker.KEY_AUTO_WALLPAPER_SEARCH_TERMS)
                        ?.split(",")?.random()?.trim()
                    photoRepository.getRandomPhoto(query = query)
                }
                else -> photoRepository.getRandomPhoto()
            }

            if (result is Success) {
                val providerClient = ProviderContract.getProviderClient(
                    applicationContext, "${BuildConfig.APPLICATION_ID}.muzeiartprovider")
                providerClient.addArtwork(result.value.toArtwork())
                return@withContext Result.success()
            } else if (result is Error && result.code == 404) {
                return@withContext Result.failure()
            } else {
                return@withContext Result.retry()
            }
        } catch (e: Throwable) {
            return@withContext Result.failure()
        }
    }

    private fun Photo.toArtwork() = Artwork(
        token = id,
        title = description,
        byline = user?.name,
        persistentUri = urls.full.toUri(),
        webUri = links?.html?.toUri(),
        metadata = user?.links?.html
    )

    companion object {

        fun scheduleJob(context: Context) {
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<MuzeiWorker>().setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build())
        }
    }
}