package de.dertyp7214.rboardthememanager.utils

import android.content.Context
import de.dertyp7214.rboardthememanager.utils.FileUtils.getSoundPacksPath
import java.io.File

object SoundUtils {
    fun loadPreviewSounds(context: Context): List<File> {
        val soundDir = File(getSoundPacksPath(context), "previews")

        return soundDir.listFiles()?.map { it } ?: ArrayList()
    }
}