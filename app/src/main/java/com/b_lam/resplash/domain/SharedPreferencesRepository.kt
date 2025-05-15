package com.b_lam.resplash.domain

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.b_lam.resplash.util.applyTheme
import java.util.Locale

class SharedPreferencesRepository(context: Context) {

    val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val theme: String?
        get() = sharedPreferences.getString(
            PREFERENCE_THEME_KEY,
            PREFERENCE_THEME_DEFAULT_VALUE)

    val layout: String?
        get() = sharedPreferences.getString(
            PREFERENCE_LAYOUT_KEY,
            PREFERENCE_LAYOUT_DEFAULT_VALUE)

    val downloader: String?
        get() = sharedPreferences.getString(
            PREFERENCE_DOWNLOADER_KEY,
            PREFERENCE_DOWNLOADER_DEFAULT_VALUE)

    val loadQuality: String?
        get() = sharedPreferences.getString(
            PREFERENCE_LOAD_QUALITY_KEY,
            PREFERENCE_LOAD_QUALITY_DEFAULT_VALUE)

    val downloadQuality: String?
        get() = sharedPreferences.getString(
            PREFERENCE_DOWNLOAD_QUALITY_KEY,
            PREFERENCE_DOWNLOAD_QUALITY_DEFAULT_VALUE)

    val wallpaperQuality: String?
        get() = sharedPreferences.getString(
            PREFERENCE_WALLPAPER_QUALITY_KEY,
            PREFERENCE_WALLPAPER_QUALITY_DEFAULT_VALUE)

    val longPressDownload: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_LONG_PRESS_DOWNLOAD_KEY,
            PREFERENCE_LONG_PRESS_DOWNLOAD_DEFAULT_VALUE)

    val autoWallpaperEnabled: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_AUTO_WALLPAPER_ENABLE_KEY,
            PREFERENCE_AUTO_WALLPAPER_ENABLE_DEFAULT_VALUE)

    val autoWallpaperOnWifi: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_AUTO_WALLPAPER_ON_WIFI_KEY,
            PREFERENCE_AUTO_WALLPAPER_ON_WIFI_DEFAULT_VALUE)

    val autoWallpaperCharging: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_AUTO_WALLPAPER_CHARGING_KEY,
            PREFERENCE_AUTO_WALLPAPER_CHARGING_DEFAULT_VALUE)

    val autoWallpaperIdle: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_AUTO_WALLPAPER_IDLE_KEY,
            PREFERENCE_AUTO_WALLPAPER_IDLE_DEFAULT_VALUE)

    val autoWallpaperInterval: Long
        get() = sharedPreferences.getString(
            PREFERENCE_AUTO_WALLPAPER_INTERVAL_KEY,
            PREFERENCE_AUTO_WALLPAPER_INTERVAL_DEFAULT_VALUE)?.toLong()
            ?: PREFERENCE_AUTO_WALLPAPER_INTERVAL_DEFAULT_VALUE.toLong()

    val autoWallpaperSource: String?
        get() = sharedPreferences.getString(
            PREFERENCE_AUTO_WALLPAPER_SOURCE_KEY,
            PREFERENCE_AUTO_WALLPAPER_SOURCE_DEFAULT_VALUE)

    val autoWallpaperUsername: String?
        get() = sharedPreferences.getString(
            PREFERENCE_AUTO_WALLPAPER_USERNAME_KEY,
            PREFERENCE_AUTO_WALLPAPER_USERNAME_DEFAULT_VALUE)

    val autoWallpaperSearchTerms: String?
        get() = sharedPreferences.getString(
            PREFERENCE_AUTO_WALLPAPER_SEARCH_TERMS_KEY,
            PREFERENCE_AUTO_WALLPAPER_SEARCH_TERMS_DEFAULT_VALUE)

    val autoWallpaperCrop: String?
        get() = sharedPreferences.getString(
            PREFERENCE_AUTO_WALLPAPER_CROP_KEY,
            PREFERENCE_AUTO_WALLPAPER_CROP_DEFAULT_VALUE)

    val autoWallpaperShowNotification: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_AUTO_WALLPAPER_SHOW_NOTIFICATION_KEY,
            PREFERENCE_AUTO_WALLPAPER_SHOW_NOTIFICATION_DEFAULT_VALUE)

    val autoWallpaperPersistNotification: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_AUTO_WALLPAPER_PERSIST_NOTIFICATION_KEY,
            PREFERENCE_AUTO_WALLPAPER_PERSIST_NOTIFICATION_DEFAULT_VALUE)

    val autoWallpaperPortraitModeOnly: Boolean
        get() = sharedPreferences.getBoolean(
            PREFERENCE_AUTO_WALLPAPER_PORTRAIT_MODE_ONLY_KEY,
            PREFERENCE_AUTO_WALLPAPER_PORTRAIT_MODE_ONLY_DEFAULT_VALUE)

    val autoWallpaperSelectScreen: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (sharedPreferences.getString(
                PREFERENCE_AUTO_WALLPAPER_SELECT_SCREEN_KEY,
                PREFERENCE_AUTO_WALLPAPER_SELECT_SCREEN_DEFAULT_VALUE))
            {
                "home_screen" -> WallpaperManager.FLAG_SYSTEM
                "lock_screen" -> WallpaperManager.FLAG_LOCK
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
        } else {
            -1
        }

    val autoWallpaperOrientation: String?
        get() {
            val value = sharedPreferences.getString(
                PREFERENCE_AUTO_WALLPAPER_ORIENTATION_KEY,
                PREFERENCE_AUTO_WALLPAPER_ORIENTATION_DEFAULT_VALUE)
            return when (value) {
                "any" -> null
                else -> value
            }
        }

    val autoWallpaperContentFilter: String?
        get() = sharedPreferences.getString(
            PREFERENCE_AUTO_WALLPAPER_CONTENT_FILTER_KEY,
            PREFERENCE_AUTO_WALLPAPER_CONTENT_FILTER_DEFAULT_VALUE)

    var lastFeaturedCollectionsFetch: Long
        get() = sharedPreferences.getLong(
            PREFERENCE_LAST_FEATURED_COLLECTIONS_FETCH_KEY,
            PREFERENCE_LAST_FEATURED_COLLECTIONS_FETCH_DEFAULT_VALUE)
        set(value) = sharedPreferences.edit {
            putLong(PREFERENCE_LAST_FEATURED_COLLECTIONS_FETCH_KEY, value)
        }

    var lastPopularCollectionsFetch: Long
        get() = sharedPreferences.getLong(
            PREFERENCE_LAST_POPULAR_COLLECTIONS_FETCH_KEY,
            PREFERENCE_LAST_POPULAR_COLLECTIONS_FETCH_DEFAULT_VALUE)
        set(value) = sharedPreferences.edit {
            putLong(PREFERENCE_LAST_POPULAR_COLLECTIONS_FETCH_KEY, value)
        }

    val locale: Locale?
        get() {
            val value = sharedPreferences.getString(
                PREFERENCE_LANGUAGE_KEY,
                PREFERENCE_LANGUAGE_DEFAULT_VALUE)
                ?: PREFERENCE_LANGUAGE_DEFAULT_VALUE
            return when (value) {
                "default" -> null
                else -> value.split("-").let {
                    when (it.size) {
                        2 -> Locale(it[0], it[1])
                        else -> Locale(it[0])
                    }
                }
            }
        }

    private val sharedPreferenceChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                PREFERENCE_THEME_KEY -> { applyTheme(theme) }
            }
        }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangedListener)
    }

    companion object {

        private const val PREFERENCE_LANGUAGE_KEY = "language"
        private const val PREFERENCE_LANGUAGE_DEFAULT_VALUE = "default"

        private const val PREFERENCE_THEME_KEY = "theme"
        private const val PREFERENCE_THEME_DEFAULT_VALUE = "default"

        private const val PREFERENCE_LAYOUT_KEY = "layout"
        private const val PREFERENCE_LAYOUT_DEFAULT_VALUE = "default"

        private const val PREFERENCE_DOWNLOADER_KEY = "downloader"
        private const val PREFERENCE_DOWNLOADER_DEFAULT_VALUE = "default"

        private const val PREFERENCE_LOAD_QUALITY_KEY = "load_quality"
        private const val PREFERENCE_LOAD_QUALITY_DEFAULT_VALUE = "regular"

        private const val PREFERENCE_DOWNLOAD_QUALITY_KEY = "download_quality"
        private const val PREFERENCE_DOWNLOAD_QUALITY_DEFAULT_VALUE = "full"

        private const val PREFERENCE_WALLPAPER_QUALITY_KEY = "wallpaper_quality"
        private const val PREFERENCE_WALLPAPER_QUALITY_DEFAULT_VALUE = "full"

        private const val PREFERENCE_LONG_PRESS_DOWNLOAD_KEY = "long_press_download"
        private const val PREFERENCE_LONG_PRESS_DOWNLOAD_DEFAULT_VALUE = true

        const val PREFERENCE_AUTO_WALLPAPER_ENABLE_KEY = "auto_wallpaper_enable"
        const val PREFERENCE_AUTO_WALLPAPER_ENABLE_DEFAULT_VALUE = false

        private const val PREFERENCE_AUTO_WALLPAPER_ON_WIFI_KEY = "auto_wallpaper_on_wifi"
        private const val PREFERENCE_AUTO_WALLPAPER_ON_WIFI_DEFAULT_VALUE = true

        private const val PREFERENCE_AUTO_WALLPAPER_CHARGING_KEY = "auto_wallpaper_charging"
        private const val PREFERENCE_AUTO_WALLPAPER_CHARGING_DEFAULT_VALUE = false

        private const val PREFERENCE_AUTO_WALLPAPER_IDLE_KEY = "auto_wallpaper_idle"
        private const val PREFERENCE_AUTO_WALLPAPER_IDLE_DEFAULT_VALUE = false

        private const val PREFERENCE_AUTO_WALLPAPER_INTERVAL_KEY = "auto_wallpaper_interval"
        private  const val PREFERENCE_AUTO_WALLPAPER_INTERVAL_DEFAULT_VALUE = "1440"

        private const val PREFERENCE_AUTO_WALLPAPER_SOURCE_KEY = "auto_wallpaper_source"
        private const val PREFERENCE_AUTO_WALLPAPER_SOURCE_DEFAULT_VALUE = "featured"

        private const val PREFERENCE_AUTO_WALLPAPER_USERNAME_KEY = "auto_wallpaper_username"
        private const val PREFERENCE_AUTO_WALLPAPER_USERNAME_DEFAULT_VALUE = ""

        private const val PREFERENCE_AUTO_WALLPAPER_SEARCH_TERMS_KEY = "auto_wallpaper_search_terms"
        private const val PREFERENCE_AUTO_WALLPAPER_SEARCH_TERMS_DEFAULT_VALUE = ""

        private const val PREFERENCE_AUTO_WALLPAPER_CROP_KEY = "auto_wallpaper_crop"
        private const val PREFERENCE_AUTO_WALLPAPER_CROP_DEFAULT_VALUE = "center_crop"

        private const val PREFERENCE_AUTO_WALLPAPER_SHOW_NOTIFICATION_KEY = "auto_wallpaper_show_notification"
        private const val PREFERENCE_AUTO_WALLPAPER_SHOW_NOTIFICATION_DEFAULT_VALUE = true

        private const val PREFERENCE_AUTO_WALLPAPER_PERSIST_NOTIFICATION_KEY = "auto_wallpaper_persist_notification"
        private const val PREFERENCE_AUTO_WALLPAPER_PERSIST_NOTIFICATION_DEFAULT_VALUE = false

        private const val PREFERENCE_AUTO_WALLPAPER_PORTRAIT_MODE_ONLY_KEY = "auto_wallpaper_portrait_mode_only"
        private const val PREFERENCE_AUTO_WALLPAPER_PORTRAIT_MODE_ONLY_DEFAULT_VALUE = false

        private const val PREFERENCE_AUTO_WALLPAPER_SELECT_SCREEN_KEY = "auto_wallpaper_select_screen"
        private const val PREFERENCE_AUTO_WALLPAPER_SELECT_SCREEN_DEFAULT_VALUE = "both"

        private const val PREFERENCE_AUTO_WALLPAPER_ORIENTATION_KEY = "auto_wallpaper_orientation"
        private const val PREFERENCE_AUTO_WALLPAPER_ORIENTATION_DEFAULT_VALUE = "any"

        private const val PREFERENCE_AUTO_WALLPAPER_CONTENT_FILTER_KEY = "auto_wallpaper_content_filter"
        private const val PREFERENCE_AUTO_WALLPAPER_CONTENT_FILTER_DEFAULT_VALUE = "low"

        private const val PREFERENCE_LAST_FEATURED_COLLECTIONS_FETCH_KEY = "last_featured_collections_fetch"
        private const val PREFERENCE_LAST_FEATURED_COLLECTIONS_FETCH_DEFAULT_VALUE = 0L

        private const val PREFERENCE_LAST_POPULAR_COLLECTIONS_FETCH_KEY = "last_popular_collections_fetch"
        private const val PREFERENCE_LAST_POPULAR_COLLECTIONS_FETCH_DEFAULT_VALUE = 0L
    }
}
