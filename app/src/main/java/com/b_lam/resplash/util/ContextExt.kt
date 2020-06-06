package com.b_lam.resplash.util

import android.app.Notification
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.b_lam.resplash.R
import com.b_lam.resplash.ResplashApplication.Companion.CHANNEL_ID

inline fun <reified T> Context.ofType(): T? {
    var currentContext: Context? = this
    do {
        if (currentContext is T) return currentContext
        currentContext = (currentContext as? ContextWrapper)?.baseContext
    } while (currentContext != null)
    return null
}

inline fun Context.createNotification(
    priority: Int = NotificationCompat.PRIORITY_DEFAULT,
    body: NotificationCompat.Builder.() -> Unit
): Notification {
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
    builder.setSmallIcon(R.drawable.ic_resplash_24dp)
    builder.priority = priority
    builder.body()
    return builder.build()
}

fun Context?.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    this?.let { Toast.makeText(it, text, duration).show() }

fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) =
    this?.let { Toast.makeText(it, textId, duration).show() }
