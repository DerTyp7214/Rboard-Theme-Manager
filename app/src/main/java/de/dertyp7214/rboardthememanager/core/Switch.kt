package de.dertyp7214.rboardthememanager.core

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.annotation.ColorInt
import com.google.android.material.switchmaterial.SwitchMaterial

fun SwitchMaterial.setSwitchColor(@ColorInt color: Int) {
    val thumbStates = ColorStateList(
        arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        ),
        intArrayOf(Color.BLACK, color, Color.GRAY)
    )
    thumbTintList = thumbStates
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val trackStates = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
            intArrayOf(Color.GRAY, Color.TRANSPARENT)
        )
        trackTintList = trackStates
        trackTintMode = PorterDuff.Mode.OVERLAY
    }
}