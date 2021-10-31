package com.b_lam.resplash.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toUri
import androidx.work.*
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.domain.autowallpaper.AutoWallpaperRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.util.Result.Error
import com.b_lam.resplash.util.Result.Success
import com.b_lam.resplash.util.getPhotoUrl
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MuzeiWorker(
    context: Context,
    params: WorkerParameters,
    private val photoRepository: PhotoRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO){
        try {
            val result = when (inputData.getString(KEY_MUZEI_SOURCE)) {
                AutoWallpaperWorker.Companion.Source.FEATURED -> {
                    photoRepository.getRandomPhotos(featured = true)
                }
                AutoWallpaperWorker.Companion.Source.COLLECTIONS -> {
                    val collectionId = autoWallpaperRepository.getRandomAutoWallpaperCollectionId()
                    photoRepository.getRandomPhotos(collectionId = collectionId)
                }
                AutoWallpaperWorker.Companion.Source.USER -> {
                    val username = inputData.getString(KEY_MUZEI_USERNAME)
                        ?.replace("@", "")
                    photoRepository.getRandomPhotos(username = username)
                }
                AutoWallpaperWorker.Companion.Source.SEARCH -> {
                    val query = inputData.getString(KEY_MUZEI_SEARCH_TERMS)
                        ?.split(",")?.random()?.trim()
                    photoRepository.getRandomPhotos(query = query)
                }
                else -> photoRepository.getRandomPhotos()
            }

            if (result is Success && result.value.isNotEmpty()) {
                val providerClient = ProviderContract.getProviderClient(
                    applicationContext, "${BuildConfig.APPLICATION_ID}.muzeiartprovider")
                val newArtworkList =
                    providerClient.lastAddedArtwork?.let { mutableListOf(it) } ?: mutableListOf()
                newArtworkList.addAll(result.value.map { it.toArtwork() })
                providerClient.setArtwork(newArtworkList)
                return@withContext Result.success()
            } else if ((result is Success && result.value.isEmpty()) ||
                (result is Error && result.code == 404)) {
                return@withContext Result.failure()
            } else {
                return@withContext Result.retry()
            }
        } catch (e: Throwable) {
            return@withContext Result.failure()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun Photo.toArtwork(): Artwork {
        val url = getPhotoUrl(this, inputData.getString(KEY_MUZEI_QUALITY))
        return Artwork(
            token = id,
            title = description ?: alt_description?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            } ?: "Untitled",
            byline = user?.name,
            persistentUri = url.toUri(),
            webUri = links?.html?.toUri(),
            metadata = user?.links?.html
        )
    }

    companion object {

        private const val KEY_MUZEI_QUALITY = "key_muzei_quality"
        private const val KEY_MUZEI_SOURCE = "key_muzei_source"
        private const val KEY_MUZEI_USERNAME = "key_muzei_username"
        private const val KEY_MUZEI_SEARCH_TERMS = "key_muzei_search_terms"

        fun enqueueWork(
            context: Context,
            sharedPreferencesRepository: SharedPreferencesRepository
        ) {
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<MuzeiWorker>()
                    .setConstraints(
                        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setInputData(getMuzeiWorkData(sharedPreferencesRepository))
                    .build())
        }

        private fun getMuzeiWorkData(
            sharedPreferencesRepository: SharedPreferencesRepository
        ) = workDataOf(
            KEY_MUZEI_QUALITY to sharedPreferencesRepository.wallpaperQuality,
            KEY_MUZEI_SOURCE to sharedPreferencesRepository.autoWallpaperSource,
            KEY_MUZEI_USERNAME to sharedPreferencesRepository.autoWallpaperUsername,
            KEY_MUZEI_SEARCH_TERMS to sharedPreferencesRepository.autoWallpaperSearchTerms
        )
    }
}