package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.helper.RKBDFlag
import de.dertyp7214.rboardthememanager.helper.RKBDFlagType
import de.dertyp7214.rboardthememanager.helper.ThemeHelper

class FlagsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.flags_preferences, rootKey)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        var navbarThemingPref: SwitchPreference? = null

        preferenceManager.findPreference<SwitchPreference>("flags_enable_sharing").apply {
            if (this != null) {
                navbarThemingPref = this

                val default = sharedPreferences.getBoolean("flags_enable_sharing_pref", false)

                isChecked = default

                setOnPreferenceChangeListener { preference, newValue ->
                    when (preference.key) {
                        "flags_enable_sharing" -> {
                            if (preference is SwitchPreference) {
                                if (newValue is Boolean) {
                                    ThemeHelper.applyFlag(
                                        RKBDFlag.EnableSharing,
                                        newValue,
                                        RKBDFlagType.boolean
                                    )
                                    sharedPreferences.edit {
                                        putBoolean("flags_enable_sharing_pref", newValue)
                                    }
                                }
                            }
                        }
                    }
                    true
                }
            }
        }

        preferenceManager.findPreference<SwitchPreference>("flags_nav_bar_theming").apply {
            if (this != null) {
                navbarThemingPref = this

                val default = sharedPreferences.getBoolean("flags_nav_bar_theming_pref", false)

                isChecked = default

                setOnPreferenceChangeListener { preference, newValue ->
                    when (preference.key) {
                        "flags_nav_bar_theming" -> {
                            if (preference is SwitchPreference) {
                                if (newValue is Boolean) {
                                    ThemeHelper.applyFlag(
                                        RKBDFlag.ThemedNavBarStyle,
                                        if (newValue) 2 else 0,
                                        RKBDFlagType.long
                                    )
                                    sharedPreferences.edit {
                                        putBoolean("flags_nav_bar_theming_pref", newValue)
                                    }
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