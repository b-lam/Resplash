package com.b_lam.resplash.data.service

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import com.b_lam.resplash.R
import com.b_lam.resplash.activities.AutoWallpaperActivity
import com.b_lam.resplash.data.tools.AutoWallpaperWorker

@SuppressLint("Override")
@TargetApi(Build.VERSION_CODES.N)
class WallpaperQuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        val context = this
        qsTile?.run {
            when (state) {
                Tile.STATE_ACTIVE -> {
                    AutoWallpaperWorker.scheduleAutoWallpaperJobSingle(context, true)
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