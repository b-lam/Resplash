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
import com.b_lam.resplash.GlideApp
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.photo.detail.PhotoDetailActivity

class NotificationManager(private val context: Context) {

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannels = listOf(
                NotificationChannel(DOWNLOADS_CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_LOW),
                NotificationChannel(NEXT_AUTO_WALLPAPER_CHANNEL_ID, "Next Auto Wallpaper", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    setShowBadge(false)
                },
                NotificationChannel(NEW_AUTO_WALLPAPER_CHANNEL_ID, "New Auto Wallpaper Info", NotificationManager.IMPORTANCE_MIN).apply {
                    setShowBadge(false)
                }
            )
            notificationManager.createNotificationChannels(notificationChannels)

            notificationManager.notificationChannels
                .filter { it.id in OLD_CHANNEL_IDS }
                .forEach { notificationManager.deleteNotificationChannel(it.id) }
        }
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun showTileServiceDownloadingNotification() {
        val builder = NotificationCompat.Builder(context, NEXT_AUTO_WALLPAPER_CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_resplash_24dp)
            setContentTitle(context.getString(R.string.setting_wallpaper))
            setProgress(0, 0, true)
            setTimeoutAfter(60_000)
        }
        notificationManager.notify(AUTO_WALLPAPER_TILE_NOTIFICATION_ID, builder.build())
    }

    fun showTileServiceErrorNotification() {
        val builder = NotificationCompat.Builder(context, NEXT_AUTO_WALLPAPER_CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_resplash_24dp)
            setContentTitle(context.getString(R.string.error_setting_wallpaper))
        }
        notificationManager.notify(AUTO_WALLPAPER_TILE_NOTIFICATION_ID, builder.build())
    }

    fun hideTileServiceNotification() {
        notificationManager.cancel(AUTO_WALLPAPER_TILE_NOTIFICATION_ID)
    }

    fun getProgressNotificationBuilder(fileName: String, cancelIntent: PendingIntent) =
        NotificationCompat.Builder(context, DOWNLOADS_CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_LOW
            setSmallIcon(android.R.drawable.stat_sys_download)
            setTicker("")
            setOngoing(true)
            setContentTitle(fileName)
            setProgress(0, 0, true)
            addAction(0, context.getString(R.string.cancel), cancelIntent)
        }

    fun updateProgressNotification(
        builder: NotificationCompat.Builder,
        progress: Int
    ) = builder.apply {
        setProgress(100, progress, false)
        if (progress == 100) setSmallIcon(android.R.drawable.stat_sys_download_done)
    }

    fun showDownloadCompleteNotification(fileName: String, uri: Uri) {
        val builder = NotificationCompat.Builder(context, DOWNLOADS_CHANNEL_ID).apply {
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

    fun showDownloadErrorNotification(fileName: String) {
        val builder = NotificationCompat.Builder(context, DOWNLOADS_CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_LOW
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setContentTitle(fileName)
            setContentText(context.getString(R.string.oops))
            setProgress(0, 0, false)
        }
        notificationManager.notify(fileName.hashCode(), builder.build())
    }

    fun showNewAutoWallpaperNotification(
        id: String,
        title: String?,
        subtitle: String?,
        previewUrl: String?,
        persist: Boolean
    ) {
        val builder = NotificationCompat.Builder(context, NEW_AUTO_WALLPAPER_CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_MIN
            setSmallIcon(R.drawable.ic_resplash_24dp)
            setContentIntent(getCurrentWallpaperPendingIntent(id))
            setAutoCancel(persist)
            title?.let { setContentTitle(it) }
            subtitle?.let { setContentText(it) }
            previewUrl?.let {
                val futureTarget = GlideApp.with(context).asBitmap().load(previewUrl).submit()
                setLargeIcon(futureTarget.get())
                GlideApp.with(context).clear(futureTarget)
            }
            setOngoing(persist)
        }
        notificationManager.notify(NEW_AUTO_WALLPAPER_NOTIFICATION_ID, builder.build())
    }

    fun hideNewAutoWallpaperNotification() {
        notificationManager.cancel(NEW_AUTO_WALLPAPER_NOTIFICATION_ID)
    }

    fun isNewAutoWallpaperNotificationEnabled(preferenceValue: Boolean): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NEW_AUTO_WALLPAPER_CHANNEL_ID)
            channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            preferenceValue
        }
    }

    private fun getCurrentWallpaperPendingIntent(id: String): PendingIntent {
        val intent = Intent(context, PhotoDetailActivity::class.java).apply {
            putExtra(PhotoDetailActivity.EXTRA_PHOTO_ID, id)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    private fun getViewPendingIntent(uri: Uri): PendingIntent {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndType(uri, "image/*")
        }
        val chooser = Intent.createChooser(viewIntent, "Open with")
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, chooser, flags)
    }

    companion object {

        private const val DOWNLOADS_CHANNEL_ID = "downloads_channel_id"
        const val NEW_AUTO_WALLPAPER_CHANNEL_ID = "new_auto_wallpaper_channel_id"
        private const val NEXT_AUTO_WALLPAPER_CHANNEL_ID = "next_auto_wallpaper_channel_id"

        private val OLD_CHANNEL_IDS = listOf("resplash_channel_id")

        private const val AUTO_WALLPAPER_TILE_NOTIFICATION_ID = 981
        private const val NEW_AUTO_WALLPAPER_NOTIFICATION_ID = 423
    }
}