package de.dertyp7214.rboardthememanager.utils

import com.jaredrummler.android.shell.Shell

object MagiskUtils {
    fun isMagiskInstalled(): Boolean {
        val result = Shell.run("magisk")
        return result.getStdout().startsWith("magisk", true)
    }

    fun getMagiskVersionString(): String {
        val result = Shell.run("magisk -v")
        return result.getStdout()
    }

    fun getMagiskVersionNumber(): String {
        val result = Shell.run("magisk -V")
        return result.getStdout()
    }

    fun getMagiskVersionFullString(): String {
        val result = Shell.run("magisk -c")
        return result.getStdout()
    }
}