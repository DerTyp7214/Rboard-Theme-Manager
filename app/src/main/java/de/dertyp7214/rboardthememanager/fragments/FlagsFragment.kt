package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.*
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.booleanOrNull
import de.dertyp7214.rboardthememanager.helper.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class FlagsFragment : PreferenceFragmentCompat() {

    private val defaultValues =
        getCurrentXmlValues(RKBDFile.Flags) + getCurrentXmlValues(RKBDFile.Preferences)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.flags_preferences, rootKey)

        bindPreference<SwitchPreference>("flags_emoji_fix", RKBDFlag.EmojiCompatFix, { value ->
            value == "*"
        }) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EmojiCompatFix,
                    if (newValue) "*" else "disabled",
                    RKBDFlagType.string
                )
            }
        }

        bindPreference<SwitchPreference>(
            "flags_joystick_delete",
            RKBDFlag.EnableJoystickDelete
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableJoystickDelete,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>(
            "flags_deprecate_search",
            RKBDFlag.DeprecateSearch, { value -> (value as? Boolean ?: false).not() }
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.DeprecateSearch,
                    !newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>(
            "flags_enable_sharing",
            RKBDFlag.EnableSharing
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableSharing,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>(
            "flags_nav_bar_theming",
            RKBDFlag.ThemedNavBarStyle,
            { value -> value == "2" }
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.ThemedNavBarStyle,
                    if (newValue) 2 else 1,
                    RKBDFlagType.long
                )
            }
        }

        bindPreference<SwitchPreference>(
            "flags_email_provider",
            RKBDFlag.EnableEmailProviderCompletion
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableEmailProviderCompletion,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }



        bindPreference<SwitchPreference>("flags_popup_v2", RKBDFlag.EnablePopupViewV2) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnablePopupViewV2,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>("flags_logging", RKBDFlag.Logging1) { newValue ->
            if (newValue is Boolean) {
                GlobalScope.launch {
                    ThemeHelper.loggingFlags.forEach { flag ->
                        ThemeHelper.applyFlag(flag, newValue, RKBDFlagType.boolean)
                    }
                }
            }
        }

        bindPreference<SeekBarPreference>(
            "flags_keyboard_height_ratio",
            RKBDFlag.KeyboardHeightRatio
        ) { newValue ->
            if (newValue is Int) {
                GlobalScope.launch {
                    ThemeHelper.applyFlag(
                        RKBDFlag.KeyboardHeightRatio,
                        newValue.toDouble() / 10,
                        RKBDFlagType.string,
                        RKBDFile.Preferences
                    )
                }
            }
        }

        bindPreference<SwitchPreference>(
            "flags_enable_key_border",
            RKBDFlag.EnableKeyBorder
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableKeyBorder,
                    newValue,
                    RKBDFlagType.boolean,
                    RKBDFile.Preferences
                )
            }
        }

        bindPreference<SwitchPreference>(
            "flags_enable_secondary_symbols",
            RKBDFlag.EnableSecondarySymbols
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableSecondarySymbols,
                    newValue,
                    RKBDFlagType.boolean,
                    RKBDFile.Preferences
                )
            }
        }

        bindPreference<SwitchPreference>(
            "flags_show_suggestions",
            RKBDFlag.ShowSuggestions
        ) { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.ShowSuggestions,
                    newValue,
                    RKBDFlagType.boolean,
                    RKBDFile.Preferences
                )
            }
        }

        bindPreference<EditTextPreference>(
            "flags_emoji_picker_columns",
            RKBDFlag.EmojiPickerV2Columns
        ) { newValue ->
            if (newValue is String && newValue.toLongOrNull() != null) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EmojiPickerV2Columns,
                    newValue.toLong(),
                    RKBDFlagType.long
                )
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_port_b") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                ThemeHelper.applyProp(RKBDProp.BottomPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_port_r") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                ThemeHelper.applyProp(RKBDProp.RightPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_port_l") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                ThemeHelper.applyProp(RKBDProp.LeftPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_land_b") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                ThemeHelper.applyProp(RKBDProp.BottomLandPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_land_r") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                ThemeHelper.applyProp(RKBDProp.RightLandPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_land_l") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                ThemeHelper.applyProp(RKBDProp.LeftLandPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.corner_key_l") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                ThemeHelper.applyProp(RKBDProp.BottomCorners, newValue.toDouble())
            }
        }
    }

    @SuppressLint("SdCardPath")
    private fun getCurrentXmlValues(file: RKBDFile): Map<String, Any> {

        val output = HashMap<String, Any>()

        val fileName = "/dataa/data/${Config.GBOARD_PACKAGE_NAME}/shared_prefs/${file.rawValue}"
        val xmlFile = SuFile(fileName)
        if (!xmlFile.exists()) return output

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val content = SuFileInputStream(xmlFile).bufferedReader().readText()
        val xmlInput = InputSource(StringReader(content))
        val doc = dBuilder.parse(xmlInput)

        val map = doc.getElementsByTagName("map")

        for (i in 0 until map.item(0).childNodes.length) {

            if (map.item(0).childNodes.item(i).attributes?.getNamedItem("name") != null &&
                map.item(0).childNodes.item(i).attributes?.getNamedItem("value") != null
            ) {

                val name = map.item(0).childNodes.item(i).attributes.getNamedItem("name").nodeValue
                val value =
                    map.item(0).childNodes.item(i).attributes.getNamedItem("value").nodeValue

                output[name] = value.booleanOrNull() ?: value
            } else if (map.item(0).childNodes.item(i).attributes?.getNamedItem("name") != null) {
                val name = map.item(0).childNodes.item(i).attributes.getNamedItem("name").nodeValue
                val value = map.item(0).childNodes.item(i).textContent ?: ""

                output[name] = value
            }
        }

        return output
    }

    private inline fun <reified T : Preference?> bindPreference(
        key: String,
        flag: RKBDFlag? = null,
        calculateDefault: (Any) -> Any = { value -> value },
        noinline onClick: (Any) -> Unit
    ) {
        preferenceManager.findPreference<T>(key).apply {
            if (this != null) {
                if (flag != null) {
                    when (this) {
                        is SwitchPreference -> {
                            val default =
                                (defaultValues[flag.rawValue]?.let { calculateDefault(it) }) as? Boolean
                            isChecked = default ?: false
                            setSummary(if (default == true) "Enabled" else "Disabled")
                        }
                        is DropDownPreference -> {
                            val default = (defaultValues[flag.rawValue] ?: "") as String
                            setDefaultValue(default.toLong())
                            setSummary(default)
                        }
                        is SeekBarPreference -> {
                            val default = (defaultValues[flag.rawValue] ?: "0.0") as String
                            setDefaultValue((default.toDouble()).toString())
                            setSummary("${default.toDouble()}")
                        }
                        is EditTextPreference -> {
                            val default = (defaultValues[flag.rawValue] ?: "") as String
                            setDefaultValue(default)
                            setSummary(default)
                        }
                    }
                } else {
                    when (this) {
                        is SwitchPreference -> {
                            val default = sharedPreferences.getBoolean("${key}_pref", false)
                            isChecked = default
                            setSummary(if (default) "Enabled" else "Disabled")
                        }
                        is DropDownPreference -> {
                            val default = sharedPreferences.getString("${key}_pref", "9")
                            setDefaultValue(default?.toLong())
                            setSummary("$default")

                        }
                        is SeekBarPreference -> {
                            val default = sharedPreferences.getInt("${key}_pref", 10)
                            setDefaultValue((default.toDouble() / 10).toString())
                            setSummary("${default.toDouble() / 10}")
                        }
                        is EditTextPreference -> {
                            val default = sharedPreferences.getString("${key}_pref", "")
                            setDefaultValue(default)
                            setSummary("$default")
                        }

                    }
                }

                setOnPreferenceChangeListener { preference, newValue ->
                    when (preference.key) {
                        key -> {
                            onClick(newValue)

                            when (this) {
                                is DropDownPreference -> {
                                    setSummary("$newValue")
                                }
                                is SwitchPreference -> {
                                    if (newValue is Boolean) {
                                        setSummary(if (newValue) "Enabled" else "Disabled")
                                    }
                                }
                                is SeekBarPreference -> {
                                    if (newValue is Int) {
                                        val value: Double = newValue.toDouble() / 10.toDouble()
                                        setSummary("$value")
                                    }
                                }
                                is EditTextPreference -> {
                                    if (newValue is String) {
                                        setSummary(newValue)
                                    }
                                }
                            }

                            sharedPreferences.edit {
                                when (newValue) {
                                    is Boolean -> putBoolean("${key}_pref", newValue)
                                    is Long -> putLong("${key}_pref", newValue)
                                    is String -> putString("${key}_pref", newValue)
                                    is Int -> putInt("${key}_pref", newValue)
                                }
                            }

                        }
                    }
                    true
                }
            }
        }
    }
}