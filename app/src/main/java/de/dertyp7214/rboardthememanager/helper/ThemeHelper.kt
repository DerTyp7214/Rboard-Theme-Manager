package de.dertyp7214.rboardthememanager.helper

import android.annotation.SuppressLint
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.io.File

object ThemeHelper {
    @SuppressLint("SdCardPath")
    fun applyTheme(name: String): Boolean {
        val inputPackageName = "com.google.android.inputmethod.latin"
        val fileName =
            "/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml"
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
    return Shell.getShell().newJob().add(this).exec().isSuccess
}