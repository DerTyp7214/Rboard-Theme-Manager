package de.dertyp7214.rboardthememanager.helper

import android.util.TimingLogger
import de.dertyp7214.rboardthememanager.BuildConfig

class TimeLogger(
    tag: String,
    label: String,
    enabled: Boolean = true
) {

    private val timingLogger: TimingLogger = TimingLogger(tag, label)
    private val debug = BuildConfig.DEBUG && enabled

    fun reset() {
        if (debug) timingLogger.reset()
    }

    fun addSplit(section: String) {
        if (debug) timingLogger.addSplit(section)
    }

    fun dumpToLog() {
        if (debug) timingLogger.dumpToLog()
    }
}