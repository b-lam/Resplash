package com.b_lam.resplash.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.observe
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.autowallpaper.AutoWallpaperSettingsActivity
import com.b_lam.resplash.util.NotificationManager
import com.b_lam.resplash.worker.AutoWallpaperWorker
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject

@RequiresApi(Build.VERSION_CODES.N)
class AutoWallpaperTileService: TileService(), LifecycleOwner, KoinComponent {

    private val dispatcher = ServiceLifecycleDispatcher(this)

    private val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    private val notificationManager: NotificationManager by inject()

    override fun onClick() {
        qsTile?.let {
            when (it.state) {
                Tile.STATE_ACTIVE -> {
                    notificationManager.showTileServiceDownloadingNotification()
                    AutoWallpaperWorker.scheduleSingleAutoWallpaperJob(this@AutoWallpaperTileService, get())
                }
                else -> unlockAndRun {
                    Intent(this@AutoWallpaperTileService, AutoWallpaperSettingsActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivityAndCollapse(this)
                    }
                }
            }
        }
    }

    override fun onStartListening() {
        qsTile?.apply {
            if (sharedPreferencesRepository.autoWallpaperEnabled) {
                state = Tile.STATE_ACTIVE
                label = getString(R.string.auto_wallpaper_next_wallpaper)
                icon = Icon.createWithResource(this@AutoWallpaperTileService, R.drawable.ic_skip_next_24dp)
            } else {
                state = Tile.STATE_INACTIVE
                label = getString(R.string.auto_wallpaper_activate)
                icon = Icon.createWithResource(this@AutoWallpaperTileService, R.drawable.ic_compare_24dp)
            }
            updateTile()
            observeAutoWallpaperWorker()
        }
    }

    private fun observeAutoWallpaperWorker() {
        WorkManager.getInstance(this@AutoWallpaperTileService)
            .getWorkInfosForUniqueWorkLiveData(AutoWallpaperWorker.AUTO_WALLPAPER_SINGLE_JOB_ID)
            .observe(this@AutoWallpaperTileService) {
                if (it.isNotEmpty()) {
                    when (it?.first()?.state) {
                        WorkInfo.State.BLOCKED, WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING ->
                            notificationManager.showTileServiceDownloadingNotification()
                        WorkInfo.State.SUCCEEDED ->
                            notificationManager.hideTileServiceNotification()
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED ->
                            notificationManager.showTileServiceErrorNotification()
                    }
                }
            }
    }

    @CallSuper
    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onBind(intent: Intent): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @Suppress("DEPRECATION")
    @CallSuper
    override fun onStart(intent: Intent?, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        notificationManager.hideTileServiceNotification()
        super.onDestroy()
    }

    override fun getLifecycle() = dispatcher.lifecycle
}