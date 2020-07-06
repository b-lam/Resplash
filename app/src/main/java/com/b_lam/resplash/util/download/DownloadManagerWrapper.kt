package com.b_lam.resplash.util.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.collection.LongSparseArray
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.b_lam.resplash.util.RESPLASH_DIRECTORY
import com.b_lam.resplash.util.error
import java.io.File

class DownloadManagerWrapper(private val context: Context) {

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    private val downloadActionMap = LongSparseArray<DownloadAction>()

    init {
        val downloadStatusReceiver = DownloadStatusReceiver()
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(downloadStatusReceiver, intentFilter)
    }

    fun downloadPhoto(url: String, fileName: String) {
        val request = createRequest(url, fileName, true)
        val downloadId = downloadManager.enqueue(request)
        downloadActionMap.put(downloadId, DownloadAction.DOWNLOAD)
    }

    fun downloadWallpaper(url: String, fileName: String) {
        val request = createRequest(url, fileName, false)
        val downloadId = downloadManager.enqueue(request)
        downloadActionMap.put(downloadId, DownloadAction.WALLPAPER)
    }

    private fun createRequest(
        url: String,
        fileName: String,
        showCompletedNotification: Boolean
    ): DownloadManager.Request {
        val destination = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Environment.DIRECTORY_PICTURES
        } else {
            Environment.DIRECTORY_DOWNLOADS
        }

        val subPath = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            "$RESPLASH_DIRECTORY${File.separator}$fileName"
        } else {
            fileName
        }

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDestinationInExternalPublicDir(destination, subPath)
            .setNotificationVisibility(
                if (showCompletedNotification)
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                else
                    DownloadManager.Request.VISIBILITY_VISIBLE
            )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            request.setVisibleInDownloadsUi(true)
            request.allowScanningByMediaScanner()
        }

        return request
    }

    fun cancelDownload(downloadId: Long) {
        downloadManager.remove(downloadId)
    }

    private inner class DownloadStatusReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L) ?: 0L
            val downloadAction = downloadActionMap.get(id) ?: return

            val query = DownloadManager.Query().apply { setFilterById(id) }
            val cursor = downloadManager.query(query)

            if (!cursor.moveToFirst()) {
                onError(cursor, id, downloadAction, "Cursor empty, this shouldn't happened")
                return
            }

            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (cursor.getInt(statusIndex) != DownloadManager.STATUS_SUCCESSFUL) {
                onError(cursor, id, downloadAction, "Download Failed")
            } else {
                onSuccess(cursor, id, downloadAction, downloadManager.getUriForDownloadedFile(id))
            }
        }

        private fun onSuccess(
            cursor: Cursor,
            id: Long,
            downloadAction: DownloadAction,
            uri: Uri
        ) {
            cursor.close()
            downloadActionMap.remove(id)

            val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
                putExtra(STATUS_SUCCESS, true)
                putExtra(DATA_ACTION, downloadAction)
                putExtra(DATA_URI, uri)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }

        private fun onError(
            cursor: Cursor,
            id: Long,
            downloadAction: DownloadAction,
            errorMessage: String
        ) {
            error("onError: $errorMessage")

            cursor.close()
            downloadManager.remove(id)
            downloadActionMap.remove(id)

            val localIntent = Intent(ACTION_DOWNLOAD_COMPLETE).apply {
                putExtra(STATUS_SUCCESS, false)
                putExtra(DATA_ACTION, downloadAction)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
        }
    }
}
