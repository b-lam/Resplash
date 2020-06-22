package com.b_lam.resplash.util

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent.*

private const val LIGHT = "light"
private const val DARK = "dark"
private const val BATTERY = "battery"
private const val DEFAULT = "default"

fun applyTheme(theme: String?) {
    when (theme) {
        LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        BATTERY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        DEFAULT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

@ColorScheme
fun getCustomTabsColorScheme(theme: String?): Int {
    return when (theme) {
        LIGHT -> COLOR_SCHEME_LIGHT
        DARK -> COLOR_SCHEME_DARK
        else -> COLOR_SCHEME_SYSTEM
    }
}

@ColorInt
fun getThemeAttrColor(context: Context, @AttrRes colorAttr: Int): Int {
    val array: TypedArray = context.obtainStyledAttributes(null, intArrayOf(colorAttr))
    return try {
        array.getColor(0, 0)
    } finally {
        array.recycle()
    }
}
