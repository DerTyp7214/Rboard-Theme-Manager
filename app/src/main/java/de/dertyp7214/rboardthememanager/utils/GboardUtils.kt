package de.dertyp7214.rboardthememanager.utils

import android.content.Context
import de.dertyp7214.rboardthememanager.Config.GBOARD_PACKAGE_NAME

object GboardUtils {
    fun getGboardVersion(context: Context): String {
        return context.packageManager.getPackageInfo(GBOARD_PACKAGE_NAME, 0).versionName
    }
}