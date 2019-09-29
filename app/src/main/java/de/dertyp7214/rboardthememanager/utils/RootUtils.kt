package de.dertyp7214.rboardthememanager.utils

import java.io.BufferedReader
import java.io.InputStreamReader

object RootUtils {
    fun checkRoot(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = BufferedReader(InputStreamReader(process!!.inputStream))
            `in`.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }
}