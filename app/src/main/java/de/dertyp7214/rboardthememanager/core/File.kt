package de.dertyp7214.rboardthememanager.core

import com.topjohnwu.superuser.io.SuFileInputStream
import de.dertyp7214.rboardthememanager.data.ModuleMeta
import java.io.File
import kotlin.text.Charsets.UTF_8

fun File.parseModuleMeta(): ModuleMeta {
    val text = SuFileInputStream(this).readBytes().toString(UTF_8)
    val map = HashMap<String, Any>()
    text.split("\n").forEach {
        val line = it.split("=")
        if (line.size > 1) map[line[0]] = line[1]
    }
    return ModuleMeta(
        (map["id"] ?: "") as String,
        (map["name"] ?: "") as String,
        (map["version"] ?: "") as String,
        (map["versionCode"] ?: "") as String,
        (map["author"] ?: "") as String,
        (map["description"] ?: "") as String
    )
}