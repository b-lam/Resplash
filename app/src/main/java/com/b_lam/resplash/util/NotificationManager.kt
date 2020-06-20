package com.b_lam.resplash.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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

    companion object {

        private const val CHANNEL_ID = "resplash_channel_id"

        private const val TILE_SERVICE_NOTIFICATION_ID = 981
    }
}