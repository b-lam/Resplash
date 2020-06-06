package com.b_lam.resplash.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

@Suppress("UnsafeCast") // Per LayoutInflater documentation, this will always return "this", a ViewGroup
fun ViewGroup.inflateViewAndAttach(@LayoutRes layoutId: Int) = inflateView(layoutId, true) as ViewGroup

@Suppress("UnsafeCast")
inline fun <reified T : View> ViewGroup.inflateViewOfTypeWithoutAttaching(@LayoutRes layoutId: Int): T =
    inflateView(layoutId, false) as T

@Suppress("UnsafeCast")
inline fun <reified T : View> ViewGroup.inflateViewOfTypeAndAttach(@LayoutRes layoutId: Int): T =
    (inflateView(layoutId, false) as T).also { addView(it) }

fun ViewGroup.inflateView(@LayoutRes layoutId: Int, attachToRoot: Boolean): View = LayoutInflater.from(context).inflate(
    layoutId,
    this,
    attachToRoot
)
