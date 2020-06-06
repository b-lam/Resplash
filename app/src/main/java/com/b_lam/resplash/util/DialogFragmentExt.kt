package com.b_lam.resplash.util

import android.os.Build
import android.view.View
import androidx.fragment.app.DialogFragment

fun DialogFragment.changeNavigationIconColor(isLight: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        dialog?.window?.let {
            var flags = it.decorView.systemUiVisibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = if (isLight) {
                    flags xor View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
            it.decorView.systemUiVisibility = flags
        }
    }
}

fun DialogFragment.changeStatusBarIconColor(isLight: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        dialog?.window?.let {
            var flags = it.decorView.systemUiVisibility
            flags = if (isLight) {
                flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            it.decorView.systemUiVisibility = flags
        }
    }
}
