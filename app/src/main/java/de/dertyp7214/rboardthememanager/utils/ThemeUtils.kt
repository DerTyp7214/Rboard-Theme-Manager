package de.dertyp7214.rboardthememanager.utils

import android.content.Context
import android.graphics.BitmapFactory
import de.dertyp7214.rboardthememanager.Config.THEME_LOCATION
import de.dertyp7214.rboardthememanager.Config.themeCount
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.utils.FileUtils.getThemePacksPath
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object ThemeUtils {
    fun loadThemes(): List<ThemeDataClass> {
        val themeDir =
            File("/$THEME_LOCATION")
        return themeDir.listFiles()?.filter {
            it.name.toLowerCase(Locale.ROOT).endsWith(".zip")
        }?.map {
            val imageFile = File(THEME_LOCATION, it.name.removeSuffix(".zip"))
            if (imageFile.exists()) ThemeDataClass(
                BitmapFactory.decodeFile(imageFile.path),
                it.name.removeSuffix(".zip"),
                it.absolutePath
            )
            else ThemeDataClass(null, it.name.removeSuffix(".zip"), it.absolutePath)
        }.apply { if (this != null) themeCount = size } ?: ArrayList()
    }

    fun loadPreviewThemes(context: Context): List<ThemeDataClass> {
        val themeDir = File(getThemePacksPath(context), "previews")

        return themeDir.listFiles()?.filter {
            it.name.toLowerCase(Locale.ROOT).endsWith("zip")
        }?.map {
            val imageFile = File(themeDir.absolutePath, it.name.removeSuffix(".zip"))
            if (imageFile.exists()) ThemeDataClass(
                BitmapFactory.decodeFile(imageFile.path),
                it.name.removeSuffix(".zip"),
                it.absolutePath
            )
            else ThemeDataClass(null, it.name.removeSuffix(".zip"), it.absolutePath)
        }.apply { if (this != null) themeCount = size } ?: ArrayList()
    }
}