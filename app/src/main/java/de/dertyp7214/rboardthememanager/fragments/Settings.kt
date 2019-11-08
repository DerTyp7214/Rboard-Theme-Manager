package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.dertyp7214.rboardthememanager.R

class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        val values = resources.getStringArray(R.array.theme_prefs)
        var pref: DropDownPreference? = null

        fun parseValue(index: Int): String {
            return when (index) {
                -1 -> values[0]
                0 -> values[3]
                1 -> values[1]
                2 -> values[2]
                else -> values[3]
            }
        }

        fun parseIndex(index: Int): Int {
            return when (index) {
                -1 -> 0
                0 -> 3
                1 -> 1
                2 -> 2
                else -> 3
            }
        }

        fun apply(index: Int) {
            pref?.summary = parseValue(index)
            sharedPreferences.edit { putInt("theme_pref", index) }
            AppCompatDelegate.setDefaultNightMode(index)
        }

        preferenceManager.findPreference<DropDownPreference>("theme").apply {
            if (this != null) {
                pref = this
                val default =
                    sharedPreferences.getInt("theme_pref", AppCompatDelegate.getDefaultNightMode())
                summary = parseValue(default)
                setValueIndex(parseIndex(default))
                setOnPreferenceChangeListener { preference, newValue ->
                    when (preference.key) {
                        "theme" -> {
                            if (preference is DropDownPreference) {
                                when (newValue) {
                                    parseValue(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) -> apply(
                                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                    )
                                    parseValue(AppCompatDelegate.MODE_NIGHT_NO) -> apply(
                                        AppCompatDelegate.MODE_NIGHT_NO
                                    )
                                    parseValue(AppCompatDelegate.MODE_NIGHT_YES) -> apply(
                                        AppCompatDelegate.MODE_NIGHT_YES
                                    )
                                    parseValue(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY) -> apply(
                                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                                    )
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