package de.dertyp7214.rboardthememanager.utils

import com.topjohnwu.superuser.Shell

object RootUtils {
    fun <E> runWithRoot(run: () -> E): Throwable<E> {
        val triple = newThrowable<E>()
        val throwable = triple.first
        val callback = triple.second
        triple.third
        if (Shell.rootAccess()) {
            run()
        } else {
            callback?.invoke("No root access!")
        }
        return throwable
    }
}