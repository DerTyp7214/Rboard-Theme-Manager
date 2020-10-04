package de.dertyp7214.rboardthememanager.core

import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.Shell

fun String.runAsCommand(): Boolean {
    return Shell.su(this).exec().apply {
        if (err.size > 0) Logger.log(
            Logger.Companion.Type.ERROR, "RUN COMMAND",
            err.toTypedArray().contentToString()
        )
        if (out.size > 0) Logger.log(
            Logger.Companion.Type.DEBUG, "RUN COMMAND",
            out.toTypedArray().contentToString()
        )
    }.isSuccess.apply {
        Logger.log(Logger.Companion.Type.INFO, "RUN COMMAND", "${this@runAsCommand} -> $this")
    }
}

fun String.booleanOrNull(): Boolean? {
    return if (this == "true" || this == "false") toBoolean() else null
}