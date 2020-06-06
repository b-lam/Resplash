package com.b_lam.resplash.util.downloadmanager

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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Wrapper for DownloadManager using Rx
 */
class RxDownloadManager(private val context: Context) {

    private var _downloadManager: DownloadManager? = null
    private val downloadManager: DownloadManager
        get() {
            if (_downloadManager == null) {
                _downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            }
            return _downloadManager ?: throw RuntimeException("Can't get DownloadManager system service")
        }

    private val subjectMap = LongSparseArray<PublishSubject<Uri>>()

    init {
        val downloadStatusReceiver = DownloadStatusReceiver()
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(downloadStatusReceiver, intentFilter)
    }

    fun downloadPhoto(url: String, fileName: String): Pair<Long, Observable<Uri>> {
        return download(createRequest(url, fileName, true))
    }

    fun downloadWallpaper(url: String, fileName: String): Pair<Long, Observable<Uri>> {
        return download(createRequest(url, fileName, false))
    }

    private fun download(
        request: DownloadManager.Request
    ): Pair<Long, Observable<Uri>> {
        val downloadId = downloadManager.enqueue(request)
        val publishSubject = PublishSubject.create<Uri>()
        subjectMap.put(downloadId, publishSubject)
        return Pair(downloadId, publishSubject)
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
            "$RESPLASH_DOWNLOAD_FOLDER_NAME/$fileName"
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
            val publishSubject = subjectMap.get(id) ?: return

            val query = DownloadManager.Query().apply { setFilterById(id) }
            val cursor = downloadManager.query(query)

            if (!cursor.moveToFirst()) {
                removeDownloadWithError(cursor, id, publishSubject, "Cursor empty, this shouldn't happened")
                return
            }

            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (cursor.getInt(statusIndex) != DownloadManager.STATUS_SUCCESSFUL) {
                removeDownloadWithError(cursor, id, publishSubject, "Download Failed")
                return
            }

            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val downloadedFilePath = cursor.getString(uriIndex).removePrefix("file://")
            cursor.close()

            publishSubject.onNext(downloadManager.getUriForDownloadedFile(id))
            publishSubject.onComplete()
            subjectMap.remove(id)
        }

        private fun removeDownloadWithError(
            cursor: Cursor,
            id: Long,
            publishSubject: PublishSubject<Uri>,
            errorMessage: String
        ) {
            cursor.close()
            downloadManager.remove(id)
            publishSubject.onError(IllegalStateException(errorMessage))
            subjectMap.remove(id)
        }
    }

    companion object {

        const val RESPLASH_DOWNLOAD_FOLDER_NAME = "Resplash"
    }
}
