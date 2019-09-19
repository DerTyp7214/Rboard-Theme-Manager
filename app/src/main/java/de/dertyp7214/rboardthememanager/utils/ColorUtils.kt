package de.dertyp7214.rboardthememanager.utils

import android.graphics.Bitmap
import androidx.core.graphics.ColorUtils


object ColorUtils {
    fun dominantColor(image: Bitmap): Int {
        val newBitmap = Bitmap.createScaledBitmap(image, 1, 1, true)
        val color = newBitmap.getPixel(0, 0)
        newBitmap.recycle()
        return color
    }

    fun isColorLight(color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) > .5
    }
}