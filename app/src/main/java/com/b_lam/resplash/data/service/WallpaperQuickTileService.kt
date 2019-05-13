package com.b_lam.resplash.data.service

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.CallSuper
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.b_lam.resplash.R
import com.b_lam.resplash.Resplash
import com.b_lam.resplash.activities.AutoWallpaperActivity
import com.b_lam.resplash.data.tools.AutoWallpaperWorker


@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
class WallpaperQuickTileService : TileService(), LifecycleOwner {

    private val QUICK_TILE_SERVICE_NOTIFICATION_ID = 444

    private val dispatcher = ServiceLifecycleDispatcher(this)

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    @CallSuper
    override fun onStart(intent: Intent, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    @CallSuper
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return dispatcher.lifecycle
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        val context = this
        qsTile?.run {
            when (state) {
                Tile.STATE_ACTIVE -> {
                    AutoWallpaperWorker.scheduleAutoWallpaperJobSingle(context)
                    WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(
                            AutoWallpaperWorker.AUTO_WALLPAPER_SINGLE_JOB_ID)
                            .observe(this@WallpaperQuickTileService, Observer { workInfos ->
                                if (workInfos != null) {
                                    if (workInfos[0].state == WorkInfo.State.RUNNING) {
                                        val builder = NotificationCompat.Builder(applicationContext, Resplash.NOTIFICATION_CHANNEL_ID)
                                                .setSmallIcon(R.drawable.ic_resplash_notification)
                                                .setContentTitle(getString(R.string.setting_wallpaper))
                                                .setProgress(0, 0, true)
                                                .setPriority(NotificationCompat.PRIORITY_LOW)

                                        with(NotificationManagerCompat.from(applicationContext)) {
                                            notify(QUICK_TILE_SERVICE_NOTIFICATION_ID, builder.build())
                                        }
                                    } else {
                                        with(NotificationManagerCompat.from(applicationContext)) {
                                            cancel(QUICK_TILE_SERVICE_NOTIFICATION_ID)
                                        }
                                    }
                                }
                            })
                } else -> {
                    // Inactive means we attempt to activate Auto Wallpaper
                    try {
                        startActivityAndCollapse(Intent(context, AutoWallpaperActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (_: ActivityNotFoundException) {

                    }
                }
            }
        }
    }

    private fun updateTile() {
        val context = this
        qsTile?.apply {
            when {
                getAutoWallpaperServiceStatus() -> {
                    state = Tile.STATE_ACTIVE
                    label = getString(R.string.next_wallpaper)
                    icon = Icon.createWithResource(context, R.drawable.baseline_skip_next_24)
                }
                !getAutoWallpaperServiceStatus() -> {
                    // If the wallpaper isn't active, the quick tile will activate it
                    state = Tile.STATE_INACTIVE
                    label = getString(R.string.activate)
                    icon = Icon.createWithResource(context, R.drawable.baseline_compare_black_24)
                } else -> {
                    state = Tile.STATE_UNAVAILABLE
                    label = getString(R.string.auto_wallpaper_title)
                    icon = Icon.createWithResource(context, R.drawable.baseline_compare_black_24)
                }
            }
        }?.updateTile()
    }

    private fun getAutoWallpaperServiceStatus(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getBoolean("auto_wallpaper", false)
    }
}