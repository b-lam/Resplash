package com.b_lam.resplash.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.ArrayMap
import androidx.core.app.JobIntentService
import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.util.error
import com.b_lam.resplash.util.info
import com.b_lam.resplash.util.save
import com.b_lam.resplash.util.writeToFile
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.io.File

class DownloadJobIntentService : JobIntentService(), CoroutineScope by MainScope() {

    private val jobMap = ArrayMap<String, Job>()

    private val downloadService: DownloadService by inject()

    override fun onHandleWork(intent: Intent) {

        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return

        if (intent.getBooleanExtra(EXTRA_CANCEL_DOWNLOAD, false)) {
            launch { cancelJob(fileName) }
        } else {
            val url = intent.getStringExtra(EXTRA_URL) ?: return
            launch(CoroutineExceptionHandler { _, e ->
                error("CoroutineExceptionHandler", e)
            }) {
                download(fileName, url)
            }.also {
                jobMap[fileName] = it
            }
        }
    }

    private suspend fun cancelJob(fileName: String) {
        jobMap[fileName]?.let {
            it.cancelAndJoin()
            jobMap.remove(fileName)
        }
    }

    private suspend fun download(
        fileName: String,
        url: String
    ) = withContext(Dispatchers.IO) {
        try {
            val responseBody = withTimeout(DOWNLOAD_TIMEOUT_MS) {
                downloadService.downloadFile(url)
            }

            val tempFile = File.createTempFile(fileName, null, this@DownloadJobIntentService.cacheDir)

            responseBody.writeToFile(tempFile, this) {
                onProgress(fileName, it)
            }

            val uri = tempFile.save(this@DownloadJobIntentService, fileName)

            // TODO: Delete temp file

            onSuccess(fileName, uri)
        } catch (e: CancellationException) {
            onError(fileName, e, false)
        } catch (e: Exception) {
            onError(fileName, e, true)
        }

        // TODO: Use Rx or LiveData to notify observers

        // TODO: Remove job from map
    }

    private fun onProgress(fileName: String, progress: Int) {
        info("onProgress: $fileName - $progress")
        // TODO: Show progress notification
    }

    private fun onSuccess(fileName: String, uri: Uri?) {
        info("onSuccess: $fileName")
        // TODO: Show complete notification
    }

    private fun onError(fileName: String, exception: Exception, showNotification: Boolean) {
        error("onError: $fileName", exception)
        if (showNotification) {
            // TODO: Show error notification
        }
    }

    companion object {

        private const val DOWNLOAD_JOB_ID = 4444

        private const val EXTRA_FILE_NAME = "extra_file_name"
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_CANCEL_DOWNLOAD = "extra_cancel_download"

        private const val DOWNLOAD_TIMEOUT_MS = 120_000L

        fun enqueueDownload(context: Context, fileName: String, url: String) {
            val intent = Intent(context, DownloadJobIntentService::class.java).apply {
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_URL, url)
            }
            enqueueWork(context, DownloadJobIntentService::class.java, DOWNLOAD_JOB_ID, intent)
        }

        fun cancelDownload(context: Context, fileName: String) {
            val intent = Intent(context, DownloadJobIntentService::class.java).apply {
                putExtra(EXTRA_CANCEL_DOWNLOAD, true)
                putExtra(EXTRA_FILE_NAME, fileName)
            }
            enqueueWork(context, DownloadJobIntentService::class.java, DOWNLOAD_JOB_ID, intent)
        }
    }
}