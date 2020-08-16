package de.dertyp7214.rboardthememanager.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.dertyp7214.logs.helpers.Logger
import com.jaredrummler.android.shell.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import de.dertyp7214.rboardthememanager.Application
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.Config.GBOARD_PACKAGE_NAME
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.moveToCache
import de.dertyp7214.rboardthememanager.core.runAsCommand
import de.dertyp7214.rboardthememanager.data.ModuleMeta
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.utils.FileUtils.getThemePacksPath
import de.dertyp7214.rboardthememanager.utils.MagiskUtils
import java.io.File
import java.nio.charset.Charset

enum class RKBDFlagType(val rawValue: String) {
    boolean("boolean"),
    long("long"),
    string("string")
}

enum class RKBDFlag(val rawValue: String) {
    EmojiCompatFix("emoji_compat_app_whitelist"),
    EnableJoystickDelete("enable_joystick_delete"),
    DeprecateSearch("deprecate_search"),
    ThemedNavBarStyle("themed_nav_bar_style"),
    EnableSharing("enable_sharing"),
    EnableEmailProviderCompletion("enable_email_provider_completion"),
    EmojiPickerV2Columns("emojipickerv2_columns"),
    EnablePopupViewV2("enable_popup_view_v2"),

    //Loggging flags
    Logging1("log_all_sticker_shares_to_training_cache"),
    Logging2("log_all_gif_shares_to_training_cache"),
    Logging3("log_all_emoji_shares_to_training_cache"),
    Logging4("log_all_emoji_shares_to_training_cache"),
    Logging5("log_all_emoji_shares_to_training_cache"),
    Logging6("log_all_emoji_shares_to_training_cache"),
    Logging7("log_all_emoji_shares_to_training_cache"),
    Logging8("log_all_emoji_shares_to_training_cache"),
    Logging9("log_all_emoji_shares_to_training_cache"),
    Logging10("log_all_emoji_shares_to_training_cache"),
    Logging11("log_all_emoji_shares_to_training_cache"),
    Logging12("log_all_emoji_shares_to_training_cache"),
    Logging13("log_all_emoji_shares_to_training_cache"),

    // Preference flags
    KeyboardHeightRatio("keyboard_height_ratio"),
    EnableKeyBorder("enable_key_border"),
    EnableSecondarySymbols("enable_secondary_symbols"),
    ShowSuggestions("show_suggestions")
}

enum class RKBDFile(val rawValue: String) {
    Flags("flag_value.xml"),
    Preferences("com.google.android.inputmethod.latin_preferences.xml")
}

enum class RKBDProp(val rawValue: String) {
    BottomPadding("ro.com.google.ime.kb_pad_port_b"),
    RightPadding("ro.com.google.ime.kb_pad_port_r"),
    LeftPadding("ro.com.google.ime.kb_pad_port_l"),
    BottomLandPadding("ro.com.google.ime.kb_pad_land_b"),
    RightLandPadding("ro.com.google.ime.kb_pad_land_r"),
    LeftLandPadding("ro.com.google.ime.kb_pad_land_l"),
    BottomCorners("ro.com.google.ime.corner_key_l")
}

object ThemeHelper {

    fun installTheme(zip: File, move: Boolean = true): Boolean {

        if (!Shell.SU.available()) {
            return false
        }

        return if (zip.extension == "pack") {
            Application.context.let {
                if (it != null) {
                    val installDir = File(it.cacheDir, "tmpInstall")
                    val newZip = File(
                        getThemePacksPath(it).apply { if (!exists()) mkdirs() }, zip.name
                    )
                    if (!move || "cp ${zip.absolutePath} ${newZip.absoluteFile}".runAsCommand()) {
                        ZipHelper().unpackZip(installDir.absolutePath, newZip.absolutePath)
                        newZip.deleteOnExit()
                        if (installDir.isDirectory) {
                            var noError = false
                            installDir.listFiles()?.forEach { theme ->
                                if (installTheme(theme) && !noError) noError = true
                            }
                            noError
                        } else false
                    } else false
                } else false
            }
        } else {
            val installPath = SuFile(MAGISK_THEME_LOC, zip.name)
            "cp ${zip.absolutePath} ${installPath.absolutePath} && chmod 644 ${installPath.absolutePath}".runAsCommand()
        }
    }

    @SuppressLint("SdCardPath")
    fun applyTheme(name: String, withBorders: Boolean = false): Boolean {
        if (!Shell.SU.available()) {
            return false
        }
        val inputPackageName = GBOARD_PACKAGE_NAME
        val fileName =
            "/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml"
        Logger.log(Logger.Companion.Type.INFO, "APPLY", "$name $inputPackageName $fileName")
        val content = SuFileInputStream(SuFile(fileName)).use {
            it.bufferedReader().readText()
        }.let {

            var changed = it

            changed = if (changed.contains("<string name=\"additional_keyboard_theme\">"))
                changed.replace(
                    "<string name=\"additional_keyboard_theme\">.*</string>".toRegex(),
                    "<string name=\"additional_keyboard_theme\">system:$name</string>"
                )
            else
                changed.replace(
                    "<map>",
                    "<map><string name=\"additional_keyboard_theme\">system:$name</string>"
                )

            // Change enable_key_border value
            changed = if (changed.contains("<boolean name=\"enable_key_border\"")) {
                changed.replace(
                    "<boolean name=\"enable_key_border\" value=\".*\" />".toRegex(),
                    "<boolean name=\"enable_key_border\" value=\"$withBorders\" />"
                )
            } else {
                changed.replace(
                    "<map>",
                    "<map><boolean name=\"enable_key_border\" value=\"$withBorders\" />"
                )
            }

            return@let changed
        }
        SuFileOutputStream(File(fileName)).writer(Charset.defaultCharset())
            .use { outputStreamWriter ->
                outputStreamWriter.write(content)
            }

        return "am force-stop $inputPackageName".runAsCommand()
    }

    @SuppressLint("SdCardPath")
    fun getActiveTheme(): String {
        if (!Shell.SU.available()) {
            return ""
        }
        val inputPackageName = "com.google.android.inputmethod.latin"
        val fileLol =
            SuFile("/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml")
        return SuFileInputStream(fileLol).use { it.bufferedReader().readText() }
            .split("<string name=\"additional_keyboard_theme\">")
            .let { if (it.size > 1) it[1].split("</string>")[0] else "" }.replace("system:", "")
            .replace(".zip", "")
    }

    fun shareThemes(context: Activity, themes: List<ThemeDataClass>) {
        val files = ArrayList<File>()
        themes.map { it.moveToCache(context) }.forEach {
            val image = File(it.path.removeSuffix(".zip"))
            files.add(File(it.path))
            if (image.exists()) files.add(image)
        }
        val zip = File(context.cacheDir, "themes.pack")
        zip.deleteOnExit()
        ZipHelper().zip(files.map { it.absolutePath }, zip.absolutePath)
        files.forEach { it.deleteOnExit() }
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName,
            zip
        )
        ShareCompat.IntentBuilder.from(context)
            .setStream(uri)
            .setType("application/pack")
            .intent
            .setAction(Intent.ACTION_SEND)
            .setDataAndType(uri, "application/pack")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply {
                context.startActivity(
                    Intent.createChooser(
                        this,
                        context.getString(R.string.share_themes)
                    )
                )
            }
    }

    fun applyFlag(flag: RKBDFlag, value: Any, flagType: RKBDFlagType, file: RKBDFile = RKBDFile.Flags): Boolean {
        if (!Shell.SU.available()) {
            return false
        }
        val inputPackageName = GBOARD_PACKAGE_NAME
        val fileName = "data/data/$inputPackageName/shared_prefs/${file.rawValue}"
        val content = SuFileInputStream(SuFile(fileName)).use {
            it.bufferedReader().readText()
        }.let {
            var fileText = it

            if (flagType != RKBDFlagType.string) {
                fileText =
                    if (fileText.contains("<${flagType.rawValue} name=\"${flag.rawValue}\"")) {
                        fileText.replace(
                            """<${flagType.rawValue} name="${flag.rawValue}" value=".*" />""".toRegex(),
                            """<${flagType.rawValue} name="${flag.rawValue}" value="$value" />"""
                        )
                    } else {
                        fileText.replace(
                            "<map>",
                            """<map><${flagType.rawValue} name="${flag.rawValue}" value="$value" />"""
                        )
                    }
            } else {
                fileText =
                    if (fileText.contains("<${flagType.rawValue} name=\"${flag.rawValue}\">")) {
                        fileText.replace(
                            """<${flagType.rawValue} name="${flag.rawValue}">.*</string>""".toRegex(),
                            """<${flagType.rawValue} name="${flag.rawValue}">$value</string>"""
                        )
                    } else {
                        fileText.replace(
                            "<map>",
                            """<map><${flagType.rawValue} name="${flag.rawValue}">$value</string>"""
                        )
                    }
            }

            return@let fileText
        }

        SuFileOutputStream(File(fileName)).writer(Charset.defaultCharset())
            .use { outputStreamWriter ->
                outputStreamWriter.write(content)
            }

        return "am force-stop $inputPackageName".runAsCommand()
    }

    fun applyProp(prop: RKBDProp, value: Any) {
        val meta = ModuleMeta(
            Config.MODULE_ID + "_addon",
            "Rboard Themes Addon",
            "v20",
            "200",
            "RKBDI & DerTyp7214 & Nylon",
            "Addon for Rboard Themes app"
        )
        val file = mapOf(
            Pair(
                "system.prop",
                "${prop.rawValue}=$value"
            )
        )
        MagiskUtils.updateModule(meta, file)
    }

    var loggingFlags = arrayListOf(
        RKBDFlag.Logging1, RKBDFlag.Logging2, RKBDFlag.Logging3,
        RKBDFlag.Logging4,
        RKBDFlag.Logging5,
        RKBDFlag.Logging6,
        RKBDFlag.Logging7,
        RKBDFlag.Logging8,
        RKBDFlag.Logging9,
        RKBDFlag.Logging10,
        RKBDFlag.Logging11,
        RKBDFlag.Logging12,
        RKBDFlag.Logging13
    )

    fun getSoundsDirectory(): SuFile? {
        val productMedia = SuFile("/system/product/media/audio/ui/KeypressStandard.ogg")
        val systemMedia = SuFile("/system/media/audio/ui/KeypressStandard.ogg")
        return if (productMedia.exists() && productMedia.isFile) {
            SuFile("/system/product/media")
        } else if (systemMedia.exists() && systemMedia.isFile) {
            SuFile("/system/media")
        } else {
            null
        }
    }
}
