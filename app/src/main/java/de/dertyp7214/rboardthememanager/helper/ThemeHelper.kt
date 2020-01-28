package de.dertyp7214.rboardthememanager.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Environment
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import de.dertyp7214.rboardthememanager.Application
import de.dertyp7214.rboardthememanager.Config.GBOARD_PACKAGE_NAME
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.moveToCache
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import java.io.File

object ThemeHelper {

    fun installTheme(zip: File): Boolean {
        return if (zip.extension == "pack") {
            Application.context.let {
                if (it != null) {
                    val installDir = File(it.cacheDir, "tmpInstall")
                    val newZip = File(
                        File(
                            it.getExternalFilesDirs(Environment.DIRECTORY_NOTIFICATIONS)[0].absolutePath.removeSuffix(
                                "Notifications"
                            ), "ThemePacks"
                        ), zip.name
                    )
                    "mv ${zip.absolutePath} ${newZip.absoluteFile}".runAsCommand()
                    ZipHelper().unpackZip(installDir.absolutePath, newZip.absolutePath)
                    newZip.deleteOnExit()
                    if (installDir.isDirectory) {
                        var noError = false
                        installDir.listFiles()?.forEach { theme ->
                            if (installTheme(theme) && !noError) noError = true
                        }
                        noError
                    } else false
                } else false
            }
        } else {
            val installPath = SuFile(MAGISK_THEME_LOC, zip.name)
            "cp ${zip.absolutePath} ${installPath.absolutePath} && chmod 644 ${installPath.absolutePath}".runAsCommand()
        }
    }

    @SuppressLint("SdCardPath")
    fun applyTheme(name: String): Boolean {
        val inputPackageName = GBOARD_PACKAGE_NAME
        val fileName =
            "/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml"
        Logger.log(Logger.Companion.Type.INFO, "APPLY", "$name $inputPackageName $fileName")
        val content = SuFileInputStream(SuFile(fileName)).use {
            it.bufferedReader().readText()
        }.let {
            if (it.contains("<string name=\"additional_keyboard_theme\">"))
                it.replace(
                    "<string name=\"additional_keyboard_theme\">.*</string>".toRegex(),
                    "<string name=\"additional_keyboard_theme\">system:$name</string>"
                )
            else {
                it.replace(
                    "<map>",
                    "<map><string name=\"additional_keyboard_theme\">system:$name</string>"
                )
            }
        }
        SuFileOutputStream(File(fileName)).writer().use { outputStreamWriter ->
            outputStreamWriter.write(content)
        }
        return "am force-stop $inputPackageName".runAsCommand()
    }

    @SuppressLint("SdCardPath")
    fun getActiveTheme(): String {
        val inputPackageName = "com.google.android.inputmethod.latin"
        val fileLol =
            SuFile("/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml")
        return SuFileInputStream(fileLol).use { it.bufferedReader().readText() }
            .split("<string name=\"additional_keyboard_theme\">")
            .let { if (it.size > 1) it[1].split("</string>")[0] else "" }.replace("system:", "")
            .replace(".zip", "")
    }

    fun shareThemes(context: Activity, themes: List<ThemeDataClass>) {
        val files = ArrayList<File>()
        themes.map { it.moveToCache(context) }.forEach {
            val image = File(it.path.removeSuffix(".zip"))
            files.add(File(it.path))
            if (image.exists()) files.add(image)
        }
        val zip = File(context.cacheDir, "themes.pack")
        zip.deleteOnExit()
        ZipHelper().zip(files.map { it.absolutePath }, zip.absolutePath)
        files.forEach { it.deleteOnExit() }
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName,
            zip
        )
        ShareCompat.IntentBuilder.from(context)
            .setStream(uri)
            .setType("application/pack")
            .intent
            .setAction(Intent.ACTION_SEND)
            .setDataAndType(uri, "application/pack")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply {
                context.startActivity(
                    Intent.createChooser(
                        this,
                        context.getString(R.string.share_themes)
                    )
                )
            }
    }
}

fun String.runAsCommand(): Boolean {
    return Shell.getShell().newJob().add(this).exec().apply {
        if (err.size > 0) Logger.log(
            Logger.Companion.Type.ERROR, "RUN COMMAND",
            err.toTypedArray().contentToString()
        )
        if (out.size > 0) Logger.log(
            Logger.Companion.Type.DEBUG, "RUN COMMAND",
            out.toTypedArray().contentToString()
        )
    }.isSuccess.apply {
        Logger.log(Logger.Companion.Type.INFO, "RUN COMMAND", "$this ${this@runAsCommand}")
    }
}