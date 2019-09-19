package de.dertyp7214.rboardthememanager.utils

import android.graphics.BitmapFactory
import de.dertyp7214.rboardthememanager.Config.THEME_LOCATION
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object ThemeUtils {
    fun loadThemes(): List<ThemeDataClass> {
        val themeDir = File(THEME_LOCATION)
        return themeDir.listFiles()?.filter {
            it.name.toLowerCase(Locale.ROOT).endsWith(".zip")
        }?.map {
            val imageFile = File(THEME_LOCATION, it.name.removeSuffix(".zip"))
            if (imageFile.exists()) ThemeDataClass(
                BitmapFactory.decodeFile(imageFile.absolutePath),
                it.name.removeSuffix(".zip"),
                it.absolutePath
            )
            else ThemeDataClass(null, it.name.removeSuffix(".zip"), it.absolutePath)
        } ?: ArrayList()
    }
}