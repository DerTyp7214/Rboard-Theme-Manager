package de.dertyp7214.rboardthememanager.utils

import com.topjohnwu.superuser.Shell
import java.io.BufferedReader
import java.io.InputStreamReader

object RootUtils {
    fun checkRoot(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = BufferedReader(InputStreamReader(process!!.inputStream))
            `in`.readLine() != null
        } catch (t: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    fun <E> runWithRoot(run: () -> E): Throwable<E> {
        val triple = newThrowable<E>()
        val throwable = triple.first
        val callback = triple.second
        triple.third
        if (Shell.rootAccess()) {
            run()
        } else {
            callback("No root access!")
        }
        return throwable
    }
}