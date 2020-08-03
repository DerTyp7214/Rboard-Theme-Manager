package de.dertyp7214.rboardthememanager

object Config {
    const val MODULES_PATH = "/data/adb/modules"
    const val THEME_LOCATION = "system/etc/gboard_theme"
    const val MODULE_ID = "rboard-themes"
    const val MODULE_PATH = "$MODULES_PATH/$MODULE_ID"
    const val MAGISK_THEME_LOC = "$MODULE_PATH/$THEME_LOCATION"
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"

    const val PACKS_URL =
        "https://raw.githubusercontent.com/GboardThemes/Packs/master/download_list.json"

    const val SOUNDS_PACKS_URL =
        "https://raw.githubusercontent.com/GboardThemes/Soundpack/master/download_sounds.json"

    var themeCount: Int? = null
}