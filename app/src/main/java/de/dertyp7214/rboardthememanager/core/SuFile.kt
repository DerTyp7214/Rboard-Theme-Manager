package de.dertyp7214.rboardthememanager.core

import com.topjohnwu.superuser.io.SuFile
import java.io.File

fun SuFile.copy(newFile: File): Boolean {
    return "cp $absolutePath ${newFile.absolutePath}".runAsCommand()
}