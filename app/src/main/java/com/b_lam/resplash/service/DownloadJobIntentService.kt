package com.b_lam.resplash.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.util.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class DownloadJobIntentService : JobIntentService(), CoroutineScope by MainScope() {

    private val downloadService: DownloadService by inject()

    private val notificationManager: NotificationManager by inject()

    override fun onHandleWork(intent: Intent) {

        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return

        val url = intent.getStringExtra(EXTRA_URL) ?: return
        val photoId = intent.getStringExtra(EXTRA_PHOTO_ID)
        launch(CoroutineExceptionHandler { _, e ->
            error("CoroutineExceptionHandler", e)
        }) {
            download(fileName, url, photoId)
        }
    }

    private suspend fun download(
        fileName: String,
        url: String,
        photoId: String?
    ) = withContext(Dispatchers.IO) {
        try {
            val responseBody = withTimeout(DOWNLOAD_TIMEOUT_MS) {
                downloadService.downloadFile(url)
            }

            val builder = notificationManager.getProgressNotificationBuilder(fileName)

            val uri = responseBody.saveImage(this@DownloadJobIntentService, fileName, this) {
                onProgress(builder, fileName, it)
            }

            if (uri != null) {
                photoId?.let { trackDownload(photoId) }
                onSuccess(fileName, uri)
            } else {
                onError(fileName, Exception("Failed writing to file"), true)
            }
        } catch (e: CancellationException) {
            onError(fileName, e, false)
        } catch (e: Exception) {
            onError(fileName, e, true)
        }

    }

    private suspend fun trackDownload(id: String) = safeApiCall(Dispatchers.IO) {
        downloadService.trackDownload(id)
    }

    private fun onProgress(builder: NotificationCompat.Builder, fileName: String, progress: Int) {
        info("onProgress: $fileName - $progress")
        notificationManager.updateProgressNotification(builder, fileName, progress)
    }

    private fun onSuccess(fileName: String, uri: Uri) {
        info("onSuccess: $fileName - $uri")
        notificationManager.showDownloadCompleteNotification(fileName, uri)
    }

    private fun onError(fileName: String, exception: Exception, showNotification: Boolean) {
        error("onError: $fileName", exception)
        notificationManager.cancelNotification(fileName)
        if (showNotification) {
            notificationManager.showDownloadErrorNotification(fileName)
        }
    }

    companion object {

        private const val DOWNLOAD_JOB_ID = 4444

        private const val EXTRA_FILE_NAME = "extra_file_name"
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_PHOTO_ID = "extra_photo_id"

        private const val DOWNLOAD_TIMEOUT_MS = 120_000L

        fun enqueueDownload(context: Context, fileName: String, url: String, photoId: String?) {
            val intent = Intent(context, DownloadJobIntentService::class.java).apply {
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_PHOTO_ID, photoId)
            }
            enqueueWork(context, DownloadJobIntentService::class.java, DOWNLOAD_JOB_ID, intent)
        }
    }
}