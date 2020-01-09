package de.dertyp7214.rboardthememanager.core

import android.graphics.Bitmap


fun Bitmap.getDominantColor(): Int {
    val newBitmap = Bitmap.createScaledBitmap(this, 1, 1, true)
    val color = newBitmap.getPixel(0, 0)
    newBitmap.recycle()
    return color
}