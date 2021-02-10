package de.dertyp7214.rboardthememanager.helper

import android.os.Trace
import de.dertyp7214.rboardthememanager.BuildConfig

class TimeLogger(
    label: String,
    enabled: Boolean = true
) {

    private val debug = BuildConfig.DEBUG && enabled

    init {
        Trace.beginSection(label)
    }

    fun reset() {
        if (debug) Trace.endSection()
    }

    fun addSplit(section: String) {
        if (debug) {
            Trace.endSection()
            Trace.beginSection(section)
        }
    }
}