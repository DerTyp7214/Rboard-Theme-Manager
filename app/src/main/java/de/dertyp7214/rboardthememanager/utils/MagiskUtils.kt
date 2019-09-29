package de.dertyp7214.rboardthememanager.utils

import com.jaredrummler.android.shell.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileOutputStream
import de.dertyp7214.rboardthememanager.Config.MODULES_PATH
import de.dertyp7214.rboardthememanager.core.getString
import de.dertyp7214.rboardthememanager.core.parseModuleMeta
import de.dertyp7214.rboardthememanager.data.MagiskModule
import de.dertyp7214.rboardthememanager.data.ModuleMeta
import kotlin.text.Charsets.UTF_8
import com.topjohnwu.superuser.Shell as MagiskShell

object MagiskUtils {
    fun isMagiskInstalled(): Boolean {
        val result = Shell.run("magisk")
        return result.getStderr().startsWith("magisk", true)
    }

    fun getMagiskVersionString(): String {
        val result = Shell.run("magisk -v")
        return result.getStdout()
    }

    fun getMagiskVersionNumber(): String {
        val result = Shell.run("magisk -V")
        return result.getStdout()
    }

    fun getMagiskVersionFullString(): String {
        val result = Shell.run("magisk -c")
        return result.getStdout()
    }

    fun getModules(): List<MagiskModule> {
        return if (isMagiskInstalled()) {
            SuFile(MODULES_PATH).listFiles()?.filter { SuFile(it, "module.prop").exists() }?.map {
                val meta = SuFile(it, "module.prop").parseModuleMeta()
                MagiskModule(meta.id, it, meta)
            } ?: ArrayList()
        } else ArrayList()
    }

    fun installModule(meta: ModuleMeta, files: Map<String, String?>) {
        if (MagiskShell.rootAccess()) {
            val moduleDir = SuFile(MODULES_PATH, meta.id)
            moduleDir.mkdirs()
            writeSuFile(SuFile(moduleDir, "module.prop"), meta.getString())
            files.forEach {
                SuFile(moduleDir, it.key).apply {
                    if (it.value != null) writeSuFile(this, it.value ?: "")
                    else mkdirs()
                }
            }
        }
    }

    fun writeSuFile(file: SuFile, content: String) {
        SuFileOutputStream(file).use {
            it.write(content.toByteArray(UTF_8))
        }
    }
}