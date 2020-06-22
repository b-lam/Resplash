package com.b_lam.resplash.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.b_lam.resplash.R

class NotificationManager(private val context: Context) {

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun cancelNotification(fileName: String) {
        cancelNotification(fileName.hashCode())
    }

    private fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun showTileServiceDownloadingNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_resplash_24dp)
            setContentTitle(context.getString(R.string.setting_wallpaper))
            setProgress(0, 0, true)
            setTimeoutAfter(60_000)
        }
        notificationManager.notify(TILE_SERVICE_NOTIFICATION_ID, builder.build())
    }

    fun showTileServiceErrorNotification() {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_resplash_24dp)
            setContentTitle(context.getString(R.string.error_setting_wallpaper))
        }
        notificationManager.notify(TILE_SERVICE_NOTIFICATION_ID, builder.build())
    }

    fun hideTileServiceNotification() {
        notificationManager.cancel(TILE_SERVICE_NOTIFICATION_ID)
    }

    fun showDownloadCompleteNotification(fileName: String, uri: Uri) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_LOW
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setContentTitle(fileName)
            setContentText(context.getString(R.string.download_complete))
            setContentIntent(getViewPendingIntent(uri))
            setProgress(0, 0, false)
            setAutoCancel(true)
        }
        notificationManager.notify(fileName.hashCode(), builder.build())
    }

    fun getProgressNotificationBuilder(fileName: String): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_LOW
            setSmallIcon(android.R.drawable.stat_sys_download)
            setTicker("")
            setContentTitle(fileName)
            setProgress(0, 0, true)
        }
        notificationManager.notify(fileName.hashCode(), builder.build())
        return builder
    }

    fun updateProgressNotification(
        builder: NotificationCompat.Builder,
        fileName: String,
        progress: Int
    ) {
        builder.apply {
            setProgress(PROGRESS_MAX, progress, false)
        }
        notificationManager.notify(fileName.hashCode(), builder.build())
    }

    fun showDownloadErrorNotification(fileName: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_LOW
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setContentTitle(fileName)
            setContentText(context.getString(R.string.oops))
            setProgress(0, 0, false)
        }
        notificationManager.notify(fileName.hashCode(), builder.build())
    }

    private fun getViewPendingIntent(uri: Uri): PendingIntent {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(uri, "image/*")
        }

        val chooser = Intent.createChooser(viewIntent, "Open with")

        return PendingIntent.getActivity(context, 0, chooser, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {

        private const val CHANNEL_ID = "resplash_channel_id"

        private const val TILE_SERVICE_NOTIFICATION_ID = 981

        private const val PROGRESS_MAX = 100
    }
}