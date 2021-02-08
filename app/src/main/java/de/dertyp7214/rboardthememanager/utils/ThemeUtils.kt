package de.dertyp7214.rboardthememanager.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper
import com.afollestad.materialdialogs.MaterialDialog
import com.dertyp7214.logs.helpers.Logger
import com.google.android.material.button.MaterialButton
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.Application
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.Config.MODULE_ID
import de.dertyp7214.rboardthememanager.Config.THEME_LOCATION
import de.dertyp7214.rboardthememanager.Config.themeCount
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.copyRecursively
import de.dertyp7214.rboardthememanager.core.decodeBitmap
import de.dertyp7214.rboardthememanager.core.runAsCommand
import de.dertyp7214.rboardthememanager.data.ModuleMeta
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.helper.ThemeHelper
import de.dertyp7214.rboardthememanager.utils.FileUtils.getThemePacksPath
import java.io.BufferedInputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

object ThemeUtils {
    fun loadThemes(): List<ThemeDataClass> {
        val themeDir =
            SuFile(MAGISK_THEME_LOC)
        return themeDir.listFiles()?.filter {
            it.name.toLowerCase(Locale.ROOT).endsWith(".zip")
        }?.map {
            val imageFile = SuFile(MAGISK_THEME_LOC, it.name.removeSuffix(".zip"))
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(imageFile))
                else
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

    fun changeThemesPath(context: Context, path: String) {
        val oldLoc = SuFile(MAGISK_THEME_LOC)
        THEME_LOCATION = path
        val newLoc = SuFile(MAGISK_THEME_LOC)
        newLoc.mkdirs()
        oldLoc.copyRecursively(newLoc)

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

        MaterialDialog(context).show {
            setContentView(R.layout.reboot_dialog)
            findViewById<MaterialButton>(R.id.button_later).setOnClickListener { exitProcess(0) }
            findViewById<MaterialButton>(R.id.button_restart).setOnClickListener {
                "reboot".runAsCommand()
            }
        }
    }

    fun getActiveTheme(): ThemeDataClass {
        val themeName = ThemeHelper.getActiveTheme()
        return if (themeName.startsWith("assets:") && Application.context != null) {
            val imgName =
                themeName
                    .removePrefix("assets:theme_package_metadata_")
                    .removeSuffix(".binarypb")
            val image = Application.context?.let {
                try {
                    val inputStream = it.resources.openRawResource(
                        FileUtils.getResourceId(
                            it,
                            imgName,
                            "raw",
                            it.packageName
                        )
                    )
                    BitmapFactory.decodeStream(BufferedInputStream(inputStream))
                } catch (e: Exception) {
                    null
                }
            }
            ThemeDataClass(
                image,
                imgName.split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) }, ""
            )
        } else if (themeName.isNotEmpty()) {
            val image = SuFile(THEME_LOCATION, themeName)
            ThemeDataClass(
                image.decodeBitmap(),
                themeName,
                SuFile(THEME_LOCATION, "$themeName.zip").absolutePath
            )
        } else {
            if (Application.context != null) {
                val isDark =
                    (Application.context!!.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                val image = Application.context?.let {
                    val inputStream = it.resources.openRawResource(
                        FileUtils.getResourceId(
                            it,
                            if (isDark) "google_blue_dark" else "default_snapshot",
                            "raw",
                            it.packageName
                        )
                    )
                    BitmapFactory.decodeStream(BufferedInputStream(inputStream))
                }
                ThemeDataClass(
                    image,
                    "System auto",
                    "",
                    colorFilter = if (isDark) null else PorterDuffColorFilter(
                        getDeviceAccentColor(Application.context!!),
                        PorterDuff.Mode.MULTIPLY
                    )
                )
            } else ThemeDataClass(null, "", "")
        }
    }

    @JvmStatic
    fun getDeviceAccentColor(context: Context): Int {
        val value = TypedValue()
        val ctx = ContextThemeWrapper(context, android.R.style.Theme_DeviceDefault)
        ctx.theme.resolveAttribute(android.R.attr.colorAccent, value, true)
        return value.data
    }
}