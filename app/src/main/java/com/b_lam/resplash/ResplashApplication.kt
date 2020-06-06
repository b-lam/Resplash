package com.b_lam.resplash

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.b_lam.resplash.di.appModules
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.util.applyTheme
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ResplashApplication : Application() {

    private val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ResplashApplication)
            modules(appModules)
        }
        applyTheme(sharedPreferencesRepository.theme)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {

        const val CHANNEL_ID = "resplash_channel_id"
    }
}
