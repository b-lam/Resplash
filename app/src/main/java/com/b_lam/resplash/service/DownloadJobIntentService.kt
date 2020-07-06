package com.b_lam.resplash.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.SafeJobIntentService
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.util.*
import com.b_lam.resplash.util.download.*
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import org.koin.android.ext.android.inject
import java.io.File

class DownloadJobIntentService : SafeJobIntentService(), CoroutineScope by MainScope() {

    private val downloadService: DownloadService by inject()

    private val notificationManager: NotificationManager by inject()

    override fun onHandleWork(intent: Intent) {

        val action = intent.getSerializableExtra(EXTRA_ACTION) as? DownloadAction ?: return
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: return
        val url = intent.getStringExtra(EXTRA_URL) ?: return
        val photoId = intent.getStringExtra(EXTRA_PHOTO_ID)

        launch(CoroutineExceptionHandler { _, e ->
            error("CoroutineExceptionHandler", e)
        }) {
            download(action, fileName, url, photoId)
        }
    }

    private suspend fun download(
        downloadAction: DownloadAction,
        fileName: String,
        url: String,
        photoId: String?
    ) = withContext(Dispatchers.IO) {
        try {
            val builder = notificationManager.getProgressNotificationBuilder(fileName)

            val responseBody = downloadService.downloadFile(url)

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                responseBody.saveImage(this@DownloadJobIntentService, fileName) {
                    onProgress(builder, fileName, it)
                }
            } else {
                responseBody.saveImageLegacy(this@DownloadJobIntentService, fileName) {
                    onProgress(builder, fileName, it)
                }
            }

            if (uri != null) {
                photoId?.let { trackDownload(photoId) }
                onSuccess(downloadAction, fileName, uri)
            } else {
                onError(downloadAction, fileName, Exception("Failed writing to file"), true)
            }
        } catch (e: CancellationException) {
            onError(downloadAction, fileName, e, false)
        } catch (e: Exception) {
            onError(downloadAction, fileName, e, true)
        }

    }

    private suspend fun trackDownload(id: String) = safeApiCall(Dispatchers.IO) {
        downloadService.trackDownload(id)
    }

    private fun onProgress(builder: NotificationCompat.Builder, fileName: String, progress: Int) {
        notificationManager.updateProgressNotification(builder, fileName, progress)
    }

    private fun onSuccess(
        downloadAction: DownloadAction,
        fileName: String,
        uri: Uri
    ) {
        info("onSuccess: $fileName - $uri")

        val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
            putExtra(STATUS_SUCCESS, true)
            putExtra(DATA_ACTION, downloadAction)
            putExtra(DATA_URI, uri)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)

        notificationManager.cancelNotification(fileName)
        if (downloadAction == DownloadAction.DOWNLOAD) {
            notificationManager.showDownloadCompleteNotification(fileName, uri)
        }
    }

    private fun onError(
        downloadAction: DownloadAction,
        fileName: String,
        exception: Exception,
        showNotification: Boolean
    ) {
        error("onError: $fileName", exception)

        val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
            putExtra(STATUS_SUCCESS, false)
            putExtra(DATA_ACTION, downloadAction)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)

        notificationManager.cancelNotification(fileName)
        if (showNotification) {
            notificationManager.showDownloadErrorNotification(fileName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun ResponseBody.saveImage(
        context: Context,
        fileName: String,
        onProgress: ((Int) -> Unit)?
    ): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.SIZE, contentLength())
            put(MediaStore.Images.Media.RELATIVE_PATH, RESPLASH_RELATIVE_PATH)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val sink = outputStream.sink().buffer()

                val fileSize = contentLength()

                var totalBytesRead = 0L
                var progressToReport = 0

                while (true) {
                    val readCount = source().read(sink.buffer, 8192L)
                    if (readCount == -1L) break
                    sink.emit()
                    totalBytesRead += readCount
                    val progress = (100.0 * totalBytesRead / fileSize)
                    if (progress - progressToReport >= 5) {
                        progressToReport = progress.toInt()
                        onProgress?.invoke(progressToReport)
                    }
                }

                sink.close()
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        return uri
    }

    private fun ResponseBody.saveImageLegacy(
        context: Context,
        fileName: String,
        onProgress: ((Int) -> Unit)?
    ): Uri? {
        val path = File(RESPLASH_LEGACY_PATH)

        if (!path.exists()) {
            if (!path.mkdirs()) {
                return null
            }
        }

        val file = File(path, fileName)

        val sink = file.sink().buffer()

        val fileSize = contentLength()

        var totalBytesRead = 0L
        var progressToReport = 0

        while (true) {
            val readCount = source().read(sink.buffer, 8192L)
            if (readCount == -1L) break
            sink.emit()
            totalBytesRead += readCount
            val progress = (100.0 * totalBytesRead / fileSize)
            if (progress - progressToReport >= 5) {
                progressToReport = progress.toInt()
                onProgress?.invoke(progressToReport)
            }
        }

        sink.close()

        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath),
            arrayOf("image/jpeg"), null)

        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
    }

    companion object {

        private const val DOWNLOAD_JOB_ID = 4444

        private const val EXTRA_ACTION = "extra_action"
        private const val EXTRA_FILE_NAME = "extra_file_name"
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_PHOTO_ID = "extra_photo_id"

        fun enqueueDownload(
            context: Context,
            downloadAction: DownloadAction,
            fileName: String,
            url: String,
            photoId: String?
        ) {
            val intent = Intent(context, DownloadJobIntentService::class.java).apply {
                putExtra(EXTRA_ACTION, downloadAction)
                putExtra(EXTRA_FILE_NAME, fileName)
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_PHOTO_ID, photoId)
            }
            enqueueWork(context, DownloadJobIntentService::class.java, DOWNLOAD_JOB_ID, intent)
        }
    }
}