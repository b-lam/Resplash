package com.b_lam.resplash.worker

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.b_lam.resplash.data.download.DownloadService
import com.b_lam.resplash.util.*
import com.b_lam.resplash.util.download.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
import java.util.*

class DownloadWorker(
    private val context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters), KoinComponent {

    private val downloadService: DownloadService by inject()
    private val notificationManager: NotificationManager by inject()

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_INPUT_URL) ?: return Result.failure()
        val fileName = inputData.getString(KEY_OUTPUT_FILE_NAME) ?: return Result.failure()

        val downloadAction = when (inputData.getString(KEY_DOWNLOAD_ACTION)) {
            DownloadAction.DOWNLOAD.name -> DownloadAction.DOWNLOAD
            DownloadAction.WALLPAPER.name -> DownloadAction.WALLPAPER
            else -> null
        } ?: return Result.failure()

        val notificationId = id.hashCode()
        val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(id)
        val notificationBuilder =
            notificationManager.getProgressNotificationBuilder(fileName, cancelIntent)

        setForeground(ForegroundInfo(notificationId, notificationBuilder.build()))

        download(url, fileName, downloadAction, notificationId, notificationBuilder)

        return Result.success()
    }

    private suspend fun download(
        url: String,
        fileName: String,
        downloadAction: DownloadAction,
        notificationId: Int,
        notificationBuilder: NotificationCompat.Builder
    ) = withContext(Dispatchers.IO) {
        try {
            val responseBody = downloadService.downloadFile(url)

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                responseBody.saveImage(context, fileName) {
                    launch { onProgress(notificationId, notificationBuilder, it) }
                }
            } else {
                responseBody.saveImageLegacy(context, fileName) {
                    launch { onProgress(notificationId, notificationBuilder, it) }
                }
            }

            if (uri != null) {
                onSuccess(downloadAction, fileName, uri)
                inputData.getString(KEY_PHOTO_ID)?.let {
                    safeApiCall(Dispatchers.IO) { downloadService.trackDownload(it) }
                }
            } else {
                onError(downloadAction, fileName, Exception("Failed writing to file"),
                    STATUS_FAILED, true)
            }
        } catch (e: CancellationException) {
            onError(downloadAction, fileName, e, STATUS_CANCELLED, false)
        } catch (e: Exception) {
            onError(downloadAction, fileName, e, STATUS_FAILED, true)
        }
    }

    private suspend fun onProgress(
        notificationId: Int,
        builder: NotificationCompat.Builder,
        progress: Int
    ) {
        setForeground(ForegroundInfo(notificationId,
            notificationManager.updateProgressNotification(builder, progress).build()))
    }

    private fun onSuccess(
        downloadAction: DownloadAction,
        fileName: String,
        uri: Uri
    ) {
        info("onSuccess: $fileName - $uri")

        val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
            putExtra(DOWNLOAD_STATUS, STATUS_SUCCESSFUL)
            putExtra(DATA_ACTION, downloadAction)
            putExtra(DATA_URI, uri)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

        if (downloadAction == DownloadAction.DOWNLOAD) {
            notificationManager.showDownloadCompleteNotification(fileName, uri)
        }
    }

    private fun onError(
        downloadAction: DownloadAction,
        fileName: String,
        exception: Exception,
        status: Int,
        showNotification: Boolean
    ) {
        error("onError: $fileName", exception)

        val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
            putExtra(DOWNLOAD_STATUS, status)
            putExtra(DATA_ACTION, downloadAction)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

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
            val complete = resolver.openOutputStream(uri)?.use { outputStream ->
                writeToSink(outputStream.sink().buffer(), onProgress)
            } ?: false

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            if (!complete) {
                resolver.delete(uri, null, null)
                throw CancellationException("Cancelled by user")
            }
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
            if (!path.mkdirs()) return null
        }

        val file = File(path, fileName)

        val complete = writeToSink(file.sink().buffer(), onProgress)

        if (!complete && file.exists()) {
            file.delete()
            throw CancellationException("Cancelled by user")
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath),
            arrayOf("image/jpeg"), null)

        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
    }

    private fun ResponseBody.writeToSink(
        sink: BufferedSink,
        onProgress: ((Int) -> Unit)?
    ): Boolean {
        val fileSize = contentLength()

        var totalBytesRead = 0L
        var progressToReport = 0

        while (true) {
            if (isStopped) return false
            val readCount = source().read(sink.buffer, 8192L)
            if (readCount == -1L) break
            sink.emit()
            totalBytesRead += readCount
            val progress = (100.0 * totalBytesRead / fileSize)
            if (progress - progressToReport >= 10) {
                progressToReport = progress.toInt()
                onProgress?.invoke(progressToReport)
            }
        }

        sink.close()
        return true
    }

    companion object {

        const val KEY_DOWNLOAD_ACTION = "KEY_DOWNLOAD_ACTION"
        const val KEY_INPUT_URL = "KEY_INPUT_URL"
        const val KEY_OUTPUT_FILE_NAME = "KEY_OUTPUT_FILE_NAME"
        const val KEY_PHOTO_ID = "KEY_PHOTO_ID"

        fun enqueueDownload(
            context: Context,
            downloadAction: DownloadAction,
            url: String,
            fileName: String,
            photoId: String?
        ): UUID {
            val inputData = workDataOf(
                KEY_DOWNLOAD_ACTION to downloadAction.name,
                KEY_INPUT_URL to url,
                KEY_OUTPUT_FILE_NAME to fileName,
                KEY_PHOTO_ID to photoId
            )
            val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(inputData).build()
            WorkManager.getInstance(context).enqueue(request)
            return request.id
        }
    }
}