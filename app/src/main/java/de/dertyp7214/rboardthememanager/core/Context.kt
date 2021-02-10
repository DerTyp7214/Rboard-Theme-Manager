package de.dertyp7214.rboardthememanager.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed

fun Context.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun Context.getNavigationBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun delayed(delay: Long, callback: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(delay) { callback() }
}