package de.dertyp7214.rboardthememanager.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.cardview.widget.CardView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.preference.EditTextPreference
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.logs.helpers.Logger
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.topjohnwu.superuser.io.SuFileOutputStream
import de.dertyp7214.rboardthememanager.Application
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.Config.GBOARD_PACKAGE_NAME
import de.dertyp7214.rboardthememanager.Config.MAGISK_THEME_LOC
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.CustomDialogPreference
import de.dertyp7214.rboardthememanager.core.getBitmap
import de.dertyp7214.rboardthememanager.core.moveToCache
import de.dertyp7214.rboardthememanager.core.runAsCommand
import de.dertyp7214.rboardthememanager.data.ModuleMeta
import de.dertyp7214.rboardthememanager.data.PackItem
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.utils.ColorUtils
import de.dertyp7214.rboardthememanager.utils.FileUtils.getThemePacksPath
import de.dertyp7214.rboardthememanager.utils.MagiskUtils
import de.dertyp7214.rboardthememanager.utils.ThemeUtils
import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass

enum class RKBDFlagType(val rawValue: String) {
    BOOLEAN("boolean"),
    LONG("long"),
    STRING("string");

    override fun toString(): String {
        return "[${javaClass.simpleName}] $name: $rawValue"
    }
}

enum class RKBDCategory(private val category: String) {
    FLAGS_GENERAL("flags_general"),
    GBOARD_PREFERENCES("gboard_preferences"),
    GBOARD_PROPS("gboard_props");

    var file: RKBDFile? = null
        private set

    operator fun invoke() = category
    operator fun invoke(file: RKBDFile): RKBDCategory {
        this.file = file
        return this
    }
}

enum class RKBDFlag(
    val preferenceType: KClass<*>,
    val category: RKBDCategory,
    val rawValue: String,
    val key: String,
    @IdRes val title: Int,
    val defaultValue: Any,
    @IdRes val icon: Int?,
    val flagType: RKBDFlagType,
    val visible: Boolean = true,
    val props: Map<String, Any> = HashMap(),
    val parseValToVal: (value: Any) -> Any = { it }
) {
    EmojiCompatFix(
        SwitchPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "emoji_compat_app_whitelist",
        "flags_emoji_fix",
        R.string.flags_emoji_fix,
        false,
        R.drawable.ic_emoji_compat,
        RKBDFlagType.STRING,
        parseValToVal = { if (it == true) "*" else "disabled" }
    ),
    EnableJoystickDelete(
        SwitchPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "enable_joystick_delete",
        "flags_joystick_delete",
        R.string.flags_joystick_delete,
        false,
        R.drawable.ic_backspace_24px,
        RKBDFlagType.BOOLEAN
    ),
    DeprecateSearch(
        SwitchPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "deprecate_search",
        "flags_deprecate_search",
        R.string.flags_deprecate_search,
        false,
        R.drawable.ic_logo,
        RKBDFlagType.BOOLEAN,
        false,
        parseValToVal = { (it as? Boolean ?: false).not() }
    ),
    EnableSharing(
        SwitchPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "enable_sharing",
        "flags_enable_sharing",
        R.string.flags_enable_sharing,
        false,
        R.drawable.ic_share_24px,
        RKBDFlagType.BOOLEAN
    ),
    ThemedNavBarStyle(
        SwitchPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "themed_nav_bar_style",
        "flags_nav_bar_theming",
        R.string.flags_nav_bar_theming,
        false,
        R.drawable.ic_navbar,
        RKBDFlagType.LONG,
        parseValToVal = { if (it == true) 2 else 1 }
    ),
    EnableEmailProviderCompletion(
        SwitchPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "enable_email_provider_completion",
        "flags_email_provider",
        R.string.flags_email_provider,
        false,
        R.drawable.ic_mdi_alternate_email,
        RKBDFlagType.BOOLEAN
    ),
    EmojiPickerV2Columns(
        EditTextPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "emojipickerv2_columns",
        "flags_emoji_picker_columns",
        R.string.flags_emoji_picker_columns,
        "9",
        R.drawable.ic_rows,
        RKBDFlagType.LONG,
        props = mapOf(
            Pair("summary", R.string.flags_emoji_picker_columns_summary),
            Pair("dialogLayout", R.layout.preference_edit_text)
        ),
        parseValToVal = { if (it is String && it.toLongOrNull() != null) it.toLong() else it }
    ),
    EnablePopupViewV2(
        SwitchPreference::class,
        RKBDCategory.FLAGS_GENERAL,
        "enable_popup_view_v2",
        "flags_popup_v2",
        R.string.flags_popup_v2,
        false,
        R.drawable.ic_popup_v2,
        RKBDFlagType.BOOLEAN
    ),

    EnableKeyBorder(
        SwitchPreference::class,
        RKBDCategory.GBOARD_PREFERENCES(RKBDFile.Preferences),
        "enable_key_border",
        "flags_enable_key_border",
        R.string.flags_enable_key_border,
        false,
        R.drawable.ic_crop_din_24px_outlined,
        RKBDFlagType.BOOLEAN
    ),
    EnableSecondarySymbols(
        SwitchPreference::class,
        RKBDCategory.GBOARD_PREFERENCES(RKBDFile.Preferences),
        "enable_secondary_symbols",
        "flags_enable_secondary_symbols",
        R.string.flags_enable_secondary_symbols,
        false,
        R.drawable.ic_numeric,
        RKBDFlagType.BOOLEAN
    ),
    ShowSuggestions(
        SwitchPreference::class,
        RKBDCategory.GBOARD_PREFERENCES(RKBDFile.Preferences),
        "show_suggestions",
        "flags_show_suggestions",
        R.string.flags_show_suggestions,
        false,
        R.drawable.ic_alphabetical,
        RKBDFlagType.BOOLEAN
    ),
    KeyboardHeightRatio(
        CustomDialogPreference::class,
        RKBDCategory.GBOARD_PREFERENCES(RKBDFile.Preferences),
        "keyboard_height_ratio",
        "flags_keyboard_height_ratio",
        R.string.flags_keyboard_height_ratio,
        10,
        R.drawable.ic_keyboard_hide_24px_outlined,
        RKBDFlagType.STRING,
        parseValToVal = { if (it is Int) it.toDouble() / 100 else it }
    );

    override fun toString(): String {
        return "[${javaClass.simpleName}] $name: $rawValue"
    }
}

enum class RKBDFile(val rawValue: String) {
    Flags("flag_value.xml"),
    Preferences("com.google.android.inputmethod.latin_preferences.xml");

    override fun toString(): String {
        return "[${javaClass.simpleName}] $name: $rawValue"
    }
}

enum class RKBDProp(val rawValue: String) {
    BottomPadding("ro.com.google.ime.kb_pad_port_b"),
    RightPadding("ro.com.google.ime.kb_pad_port_r"),
    LeftPadding("ro.com.google.ime.kb_pad_port_l"),
    BottomLandPadding("ro.com.google.ime.kb_pad_land_b"),
    RightLandPadding("ro.com.google.ime.kb_pad_land_r"),
    LeftLandPadding("ro.com.google.ime.kb_pad_land_l"),
    BottomCorners("ro.com.google.ime.corner_key_l");

    override fun toString(): String {
        return "[${javaClass.simpleName}] $name: $rawValue"
    }
}

object ThemeHelper {
    fun installTheme(zip: File, move: Boolean = true, activity: FragmentActivity? = null): Boolean {
        return if (zip.extension == "pack") {
            Application.context.let {
                if (it != null) {
                    val installDir = SuFile(it.cacheDir, "tmpInstall")
                    val newZip = SuFile(
                        getThemePacksPath(it).apply { if (!exists()) mkdirs() }, zip.name
                    )
                    if (!move || listOf(
                            "cp ${zip.absolutePath} ${newZip.absoluteFile}",
                            "chmod 664 ${newZip.absoluteFile}"
                        ).runAsCommand()
                    ) {
                        ZipHelper().unpackZip(installDir.absolutePath, newZip.absolutePath)
                        newZip.delete()
                        if (installDir.isDirectory) {
                            var noError = true
                            val themes = ArrayList<SuFile>()
                            installDir.listFiles()?.let { files -> themes.addAll(files) }

                            activity?.let { activity ->
                                val packItem = PackItem("Shared Pack", "Rboard Theme Manager", "")
                                previewDialog(
                                    activity,
                                    installDir.absolutePath,
                                    packItem,
                                    { closeDialog ->
                                        var noErrorInstall = true
                                        themes.forEach { theme ->
                                            if (!installTheme(theme)) noErrorInstall = false
                                        }
                                        if (noErrorInstall) Toast.makeText(
                                            activity,
                                            R.string.theme_added,
                                            Toast.LENGTH_LONG
                                        ).show() else Toast.makeText(
                                            activity,
                                            R.string.error,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        closeDialog()
                                    }, {
                                        activity.finishAndRemoveTask()
                                    }) { pair ->
                                    val adapter =
                                        PreviewAdapter(
                                            activity,
                                            ArrayList(ThemeUtils.loadThemes(installDir))
                                        )

                                    pair.second.findViewById<MaterialButton>(R.id.download_button)?.isEnabled =
                                        true

                                    val recyclerView =
                                        pair.second.findViewById<RecyclerView>(R.id.preview_recyclerview)
                                    recyclerView?.layoutManager = LinearLayoutManager(activity)
                                    recyclerView?.setHasFixedSize(true)
                                    recyclerView?.adapter = adapter
                                    recyclerView?.addItemDecoration(
                                        StartOffsetItemDecoration(
                                            0
                                        )
                                    )

                                    recyclerView?.visibility = View.VISIBLE
                                    pair.first.visibility = View.GONE

                                    val bDialog = pair.second.dialog
                                    if (bDialog is BottomSheetDialog) {
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            bDialog.behavior.state =
                                                BottomSheetBehavior.STATE_EXPANDED
                                        }, 100)
                                    }
                                }
                                true
                            } ?: themes.forEach { theme ->
                                if (!installTheme(theme)) noError = false
                            }
                            noError
                        } else false
                    } else false
                } else false
            }
        } else {
            val installPath = SuFile(MAGISK_THEME_LOC, zip.name)
            listOf(
                "mkdir -p $MAGISK_THEME_LOC",
                "cp ${zip.absolutePath} ${installPath.absolutePath}",
                "chmod 644 ${installPath.absolutePath}"
            ).runAsCommand()
        }
    }

    @SuppressLint("SdCardPath")
    fun applyTheme(
        name: String,
        withBorders: Boolean = false,
        context: Context? = null
    ): Boolean {
        val inputPackageName = GBOARD_PACKAGE_NAME
        val fileName =
            "/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml"
        Logger.log(
            Logger.Companion.Type.INFO,
            "APPLY",
            "[ApplyTheme]: $name $inputPackageName $fileName"
        )
        return if (!SuFile(fileName).exists()) {
            context?.apply {
                Toast.makeText(this, R.string.please_open_app, Toast.LENGTH_LONG).show()
            }
            false
        } else {
            val content = SuFileInputStream.open(SuFile(fileName)).use {
                it.bufferedReader().readText()
            }.let {
                var changed = it

                changed = if ("<string name=\"additional_keyboard_theme\">" in changed)
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
                changed = if ("<boolean name=\"enable_key_border\"" in changed) {
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
            SuFileOutputStream.open(File(fileName)).writer(Charset.defaultCharset())
                .use { outputStreamWriter ->
                    outputStreamWriter.write(content)
                }

            "am force-stop $inputPackageName".runAsCommand()
        }
    }

    @SuppressLint("SdCardPath")
    fun getActiveTheme(): String {
        val inputPackageName = "com.google.android.inputmethod.latin"
        val fileLol =
            SuFile("/data/data/$inputPackageName/shared_prefs/${inputPackageName}_preferences.xml")
        return try {
            if (!fileLol.exists()) ""
            else SuFileInputStream.open(fileLol).bufferedReader().readText()
                .split("<string name=\"additional_keyboard_theme\">")
                .let { if (it.size > 1) it[1].split("</string>")[0] else "" }.replace("system:", "")
                .replace(".zip", "")
        } catch (error: Exception) {
            Logger.log(Logger.Companion.Type.ERROR, "ActiveTheme", error.message)
            ""
        }
    }

    fun shareThemes(context: Activity, themes: List<ThemeDataClass>) {
        val files = ArrayList<File>()
        themes.map { it.moveToCache(context) }.forEach {
            val image = File(it.path.removeSuffix(".zip"))
            files.add(File(it.path))
            if (image.exists()) files.add(image)
        }
        val zip = File(context.cacheDir, "themes.pack")
        zip.delete()
        ZipHelper().zip(files.map { it.absolutePath }, zip.absolutePath)
        files.forEach { it.deleteOnExit() }
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName,
            zip
        )
        ShareCompat.IntentBuilder(context)
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

    @SuppressLint("SdCardPath")
    fun applyFlag(
        flag: RKBDFlag,
        value: Any,
        flagType: RKBDFlagType,
        file: RKBDFile = RKBDFile.Flags
    ): Boolean {
        val inputPackageName = GBOARD_PACKAGE_NAME
        val fileName = "/data/data/$inputPackageName/shared_prefs/${file.rawValue}"
        val content = SuFileInputStream.open(SuFile(fileName)).use {
            it.bufferedReader().readText()
        }.let {
            var fileText = it

            if (flagType != RKBDFlagType.STRING) {
                fileText =
                    if ("<${flagType.rawValue} name=\"${flag.rawValue}\"" in fileText) {
                        fileText.replace(
                            """<${flagType.rawValue} name="${flag.rawValue}" value=".*" />""".toRegex(),
                            """<${flagType.rawValue} name="${flag.rawValue}" value="$value" />"""
                        )
                    } else {
                        if (Regex("<map[ |]/>") in fileText)
                            fileText.replace(
                                Regex("<map[ |]/>"),
                                """<map><${flagType.rawValue} name="${flag.rawValue}" value="$value" /></map>"""
                            )
                        else
                            fileText.replace(
                                "<map>",
                                """<map><${flagType.rawValue} name="${flag.rawValue}" value="$value" />"""
                            )
                    }
            } else {
                fileText =
                    if ("<${flagType.rawValue} name=\"${flag.rawValue}\">" in fileText) {
                        fileText.replace(
                            """<${flagType.rawValue} name="${flag.rawValue}">.*</string>""".toRegex(),
                            """<${flagType.rawValue} name="${flag.rawValue}">$value</string>"""
                        )
                    } else {
                        if (Regex("<map[ |]/>") in fileText)
                            fileText.replace(
                                Regex("<map[ |]/>"),
                                """<map><${flagType.rawValue} name="${flag.rawValue}">$value</string></map>"""
                            )
                        else
                            fileText.replace(
                                "<map>",
                                """<map><${flagType.rawValue} name="${flag.rawValue}">$value</string>"""
                            )
                    }
            }

            return@let fileText
        }

        Logger.log(Logger.Companion.Type.DEBUG, "Change Flag", "$flag | $value")
        Logger.log(Logger.Companion.Type.DEBUG, "Change Flag", "$flagType | $value")

        writeSuFile(SuFile(fileName), content)

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

    private fun writeSuFile(file: SuFile, content: String) {
        SuFileOutputStream.open(file).writer(Charset.defaultCharset())
            .use { outputStreamWriter ->
                outputStreamWriter.write(content)
            }
    }
}

class PreviewAdapter(
    private val context: Context,
    private val list: List<ThemeDataClass>
) : RecyclerView.Adapter<PreviewAdapter.ViewHolder>() {

    @SuppressLint("UseCompatLoadingForDrawables")
    private val default = context.resources.getDrawable(
        R.drawable.ic_keyboard,
        null
    ).getBitmap()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.theme_grid_item_single,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val dataClass = list[position]
        val color = ColorUtils.dominantColor(dataClass.image ?: default)
        if (holder.gradient != null) {
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color, Color.TRANSPARENT)
            )
            holder.gradient.background = gradient
        }

        holder.themeImage.setImageBitmap(dataClass.image ?: default)
        holder.themeImage.alpha = if (dataClass.image != null) 1F else .3F

        holder.themeName.text =
            dataClass.name.split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) }
        holder.themeNameSelect.text =
            dataClass.name.split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) }

        holder.themeName.setTextColor(if (ColorUtils.isColorLight(color)) Color.BLACK else Color.WHITE)

        if (dataClass.selected)
            holder.selectOverlay.alpha = 1F
        else
            holder.selectOverlay.alpha = 0F

        holder.card.setCardBackgroundColor(color)

        holder.card.setOnClickListener {
            val success = ThemeHelper.installTheme(SuFile(dataClass.path))
                    && if (dataClass.image != null) ThemeHelper.installTheme(
                SuFile(
                    dataClass.path.removeSuffix(
                        ".zip"
                    )
                )
            )
            else true
            Logger.log(
                Logger.Companion.Type.DEBUG,
                "INSTALL THEME",
                "${dataClass.name}: $success | Image: ${dataClass.image != null}"
            )
            if (success) Toast.makeText(context, R.string.theme_added, Toast.LENGTH_LONG).show()
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val themeImage: ImageView = v.findViewById(R.id.theme_image)
        val themeName: TextView = v.findViewById(R.id.theme_name)
        val themeNameSelect: TextView = v.findViewById(R.id.theme_name_selected)
        val selectOverlay: ViewGroup = v.findViewById(R.id.select_overlay)
        val card: CardView = v.findViewById(R.id.card)
        val gradient: View? = try {
            v.findViewById(R.id.gradient)
        } catch (e: Exception) {
            null
        }
    }
}
