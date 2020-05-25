package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.*
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.helper.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FlagsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.flags_preferences, rootKey)

        bindPreference<SwitchPreference>("flags_emoji_fix") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EmojiCompatFix,
                    if (newValue) "*" else "disabled",
                    RKBDFlagType.string
                )
            }
        }

        bindPreference<SwitchPreference>("flags_joystick_delete") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableJoystickDelete,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>("flags_deprecate_search") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.DeprecateSearch,
                    !newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>("flags_enable_sharing") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableSharing,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>("flags_nav_bar_theming") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.ThemedNavBarStyle,
                    if (newValue) 2 else 1,
                    RKBDFlagType.long
                )
            }
        }

        bindPreference<SwitchPreference>("flags_email_provider") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableEmailProviderCompletion,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<EditTextPreference>("flags_emoji_picker_columns") { newValue ->
            if (newValue is String && newValue.toLongOrNull() != null) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EmojiPickerV2Columns,
                    newValue.toLong(),
                    RKBDFlagType.long
                )
            }
        }

        bindPreference<SwitchPreference>("flags_popup_v2") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnablePopupViewV2,
                    newValue,
                    RKBDFlagType.boolean
                )
            }
        }

        bindPreference<SwitchPreference>("flags_logging") { newValue ->
            if (newValue is Boolean) {
                GlobalScope.launch {
                    ThemeHelper.loggingFlags.forEach { flag ->
                        ThemeHelper.applyFlag(flag, newValue, RKBDFlagType.boolean)
                    }
                }
            }
        }

        bindPreference<SeekBarPreference>("flags_keyboard_height_ratio") { newValue ->
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

        bindPreference<SwitchPreference>("flags_enable_key_border") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableKeyBorder,
                    newValue,
                    RKBDFlagType.boolean,
                    RKBDFile.Preferences
                )
            }
        }

        bindPreference<SwitchPreference>("flags_enable_secondary_symbols") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.EnableSecondarySymbols,
                    newValue,
                    RKBDFlagType.boolean,
                    RKBDFile.Preferences
                )
            }
        }

        bindPreference<SwitchPreference>("flags_show_suggestions") { newValue ->
            if (newValue is Boolean) {
                ThemeHelper.applyFlag(
                    RKBDFlag.ShowSuggestions,
                    newValue,
                    RKBDFlagType.boolean,
                    RKBDFile.Preferences
                )
            }
        }

        bindPreference<SeekBarPreference>("props_ro.com.google.ime.kb_pad_port_b") {
            newValue ->
            if (newValue is Int) {
                ThemeHelper.applyProp(RKBDProp.BottomPadding, newValue / 10)
            }
        }

        bindPreference<SeekBarPreference>("props_ro.com.google.ime.kb_pad_port_r") {
                newValue ->
            if (newValue is Int) {
                ThemeHelper.applyProp(RKBDProp.RightPadding, newValue / 10)
            }
        }

        bindPreference<SeekBarPreference>("props_ro.com.google.ime.kb_pad_port_l") {
                newValue ->
            if (newValue is Int) {
                ThemeHelper.applyProp(RKBDProp.LeftPadding, newValue / 10)
            }
        }

        bindPreference<SeekBarPreference>("props_ro.com.google.ime.kb_pad_land_b") {
                newValue ->
            if (newValue is Int) {
                ThemeHelper.applyProp(RKBDProp.BottomLandPadding, newValue.toDouble() / 10)
            }
        }

        bindPreference<SeekBarPreference>("props_ro.com.google.ime.kb_pad_land_r") {
                newValue ->
            if (newValue is Int) {
                ThemeHelper.applyProp(RKBDProp.RightLandPadding, newValue.toDouble() / 10)
            }
        }

        bindPreference<SeekBarPreference>("props_ro.com.google.ime.kb_pad_land_l") {
                newValue ->
            if (newValue is Int) {
                ThemeHelper.applyProp(RKBDProp.LeftLandPadding, newValue.toDouble() / 10)
            }
        }

        bindPreference<SeekBarPreference>("props_ro.com.google.ime.corner_key_l") {
                newValue ->
            if (newValue is Int) {
                ThemeHelper.applyProp(RKBDProp.BottomCorners, newValue.toDouble() / 10)
            }
        }
    }

    private inline fun <reified T : Preference?> bindPreference(
        key: String,
        noinline onClick: (Any) -> Unit
    ) {
        preferenceManager.findPreference<T>(key).apply {
            if (this != null) {

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
                                        var value: Double = newValue.toDouble() / 10.toDouble()
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