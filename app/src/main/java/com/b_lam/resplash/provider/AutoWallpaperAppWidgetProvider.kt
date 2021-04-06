package com.b_lam.resplash.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.autowallpaper.AutoWallpaperSettingsActivity
import com.b_lam.resplash.util.toast
import com.b_lam.resplash.worker.AutoWallpaperWorker
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class AutoWallpaperAppWidgetProvider : AppWidgetProvider(), KoinComponent {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        val sharedPreferencesRepository: SharedPreferencesRepository by inject()

        if (context != null && intent?.action == ACTION_WIDGET_NEXT) {
            with(context) {
                if (sharedPreferencesRepository.autoWallpaperEnabled) {
                    toast(R.string.setting_wallpaper)
                    AutoWallpaperWorker.scheduleSingleAutoWallpaperJob(
                        this, sharedPreferencesRepository)
                } else {
                    toast("Auto Wallpaper is not enabled")
                    Intent(this, AutoWallpaperSettingsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(this)
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.auto_wallpaper_app_widget)
        views.setOnClickPendingIntent(R.id.auto_wallpaper_next_button,
            getNextAutoWallpaperPendingIntent(context))
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getNextAutoWallpaperPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AutoWallpaperAppWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_NEXT
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {

        const val ACTION_WIDGET_NEXT = "com.b_lam.resplash.ACTION_WIDGET_NEXT"
    }
}