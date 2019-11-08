package de.dertyp7214.rboardthememanager.helper

import android.annotation.SuppressLint
import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import de.dertyp7214.rboardthememanager.Config.GBOARD_PACKAGE_NAME
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import java.io.File

object ThemeHelper {

    fun installTheme(zip: File): Boolean {
        val installPath = SuFile(MAGISK_THEME_LOC, zip.name)
        return "cp ${zip.absolutePath} ${installPath.absolutePath} && chmod 644 ${installPath.absolutePath}".runAsCommand()
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