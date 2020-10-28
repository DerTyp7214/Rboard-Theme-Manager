package de.dertyp7214.rboardthememanager.utils

import android.content.Context
import android.graphics.BitmapFactory
import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.Config.MODULE_ID
import de.dertyp7214.rboardthememanager.Config.THEME_LOCATION
import de.dertyp7214.rboardthememanager.Config.themeCount
import de.dertyp7214.rboardthememanager.core.decodeBitmap
import de.dertyp7214.rboardthememanager.core.runAsCommand
import de.dertyp7214.rboardthememanager.data.ModuleMeta
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.utils.FileUtils.getThemePacksPath
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object ThemeUtils {
    fun loadThemes(): List<ThemeDataClass> {
        val themeDir =
            SuFile(THEME_LOCATION)
        return themeDir.listFiles()?.filter {
            it.name.toLowerCase(Locale.ROOT).endsWith(".zip")
        }?.map {
            val imageFile = SuFile(THEME_LOCATION, it.name.removeSuffix(".zip"))
            if (imageFile.exists()) ThemeDataClass(
                imageFile.decodeBitmap(),
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

    fun getThemesPathFromProps(): String? {
        var path: String? = null
        "getprop ro.com.google.ime.themes_dir".runAsCommand {
            if (it.first().isNotEmpty()) path = it.first()
            Logger.log(Logger.Companion.Type.DEBUG, "Gboard-Themes-Path", path)
        }
        return path
    }

    fun checkForExistingThemes(): Boolean {
        return getThemesPathFromProps() != null
    }

    fun changeThemesPath(path: String) {
        val oldLoc = MAGISK_THEME_LOC
        THEME_LOCATION = path
        val newLoc = MAGISK_THEME_LOC
        "mkdir -p $newLoc".runAsCommand()
        "cp -a $oldLoc/. $newLoc/".runAsCommand()

        val meta = ModuleMeta(
            MODULE_ID,
            "Rboard Themes",
            "v20",
            "200",
            "RKBDI & DerTyp7214",
            "Module for Rboard Themes app"
        )
        val file = mapOf(
            Pair(
                "system.prop",
                "ro.com.google.ime.themes_dir=$THEME_LOCATION"
            )
        )
        MagiskUtils.updateModule(meta, file)
    }
}