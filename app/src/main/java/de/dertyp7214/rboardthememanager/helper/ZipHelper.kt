package de.dertyp7214.rboardthememanager.helper

import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.io.SuFileOutputStream
import java.io.File
import java.util.zip.ZipFile


class ZipHelper {
    fun unpackZip(path: String, zipName: String): Boolean {
        ZipFile(zipName).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    File(path).mkdirs()
                    SuFileOutputStream(File(path, entry.name)).use { output ->
                        Logger.log(
                            Logger.Companion.Type.DEBUG,
                            "OUTPUT",
                            File(path, entry.name).absolutePath
                        )
                        input.copyTo(output)
                    }
                }
            }
        }
        return true
    }
}