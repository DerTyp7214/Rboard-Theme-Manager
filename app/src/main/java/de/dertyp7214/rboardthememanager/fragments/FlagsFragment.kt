package de.dertyp7214.rboardthememanager.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.*
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.CustomDialogPreference
import de.dertyp7214.rboardthememanager.core.booleanOrNull
import de.dertyp7214.rboardthememanager.core.iterator
import de.dertyp7214.rboardthememanager.helper.*
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class FlagsFragment : PreferenceFragmentCompat() {

    private val defaultValues =
        getCurrentXmlValues(RKBDFile.Flags) + getCurrentXmlValues(RKBDFile.Preferences)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (defaultValues.isEmpty()) requireActivity().apply {
            preferenceManager.findPreference<PreferenceCategory>("flags_general")?.apply {
                summary = getString(R.string.no_flags)
                forEach {
                    it.isVisible = false
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.flags_preferences, rootKey)

        val flagsGeneral = preferenceManager.findPreference<PreferenceCategory>("flags_general")
        val gboardPreferences =
            preferenceManager.findPreference<PreferenceCategory>("gboard_preferences")
        val gboardProps = preferenceManager.findPreference<PreferenceCategory>("gboard_props")

        val applyFlag = { flag: RKBDFlag, value: Any ->
            val parsed = flag.parseValToVal(value)
            try {
                applyFlag(
                    flag,
                    parsed,
                    flag.flagType,
                    flag.category.file ?: RKBDFile.Flags
                )
            } catch (e: Exception) {
            }
        }

        fun <V : Preference> putDefaults(preference: V, flag: RKBDFlag): V {
            preference.setTitle(flag.title)
            preference.key = flag.key
            if (flag.icon != null) preference.setIcon(flag.icon)
            preference.setDefaultValue(flag.defaultValue)
            preference.isVisible = flag.visible

            flag.props.forEach { (k, v) ->
                when (k) {
                    "summary" -> if (v is Int) preference.setSummary(v)
                }
            }

            when (flag.category) {
                RKBDCategory.FLAGS_GENERAL ->
                    flagsGeneral?.addPreference(preference)
                RKBDCategory.GBOARD_PREFERENCES ->
                    gboardPreferences?.addPreference(preference)
                RKBDCategory.GBOARD_PROPS ->
                    gboardProps?.addPreference(preference)
            }

            return preference
        }

        RKBDFlag.values().forEach {
            when (it.preferenceType) {
                SwitchPreference::class -> {
                    putDefaults(SwitchPreference(context), it)
                    bindPreference<SwitchPreference>(
                        it.key,
                        it,
                        { value -> value == it.parseValToVal(it.defaultValue as Boolean) }) { newValue ->
                        applyFlag(it, newValue)
                    }
                }
                EditTextPreference::class -> {
                    val preference = putDefaults(EditTextPreference(context), it)

                    it.props.forEach { (k, v) ->
                        when (k) {
                            "dialogLayout" -> if (v is Int) preference.dialogLayoutResource = v
                        }
                    }
                    bindPreference<EditTextPreference>(
                        it.key,
                        it
                    ) { newValue ->
                        applyFlag(it, newValue)
                    }
                }
                CustomDialogPreference::class -> {
                    putDefaults(CustomDialogPreference(context), it)
                    bindPreference<CustomDialogPreference>(
                        it.key,
                        it
                    ) { newValue ->
                        applyFlag(it, newValue)
                    }
                }
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_port_b") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                applyProp(RKBDProp.BottomPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_port_r") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                applyProp(RKBDProp.RightPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_port_l") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                applyProp(RKBDProp.LeftPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_land_b") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                applyProp(RKBDProp.BottomLandPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_land_r") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                applyProp(RKBDProp.RightLandPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.kb_pad_land_l") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                applyProp(RKBDProp.LeftLandPadding, newValue.toDouble())
            }
        }

        bindPreference<EditTextPreference>("props_ro.com.google.ime.corner_key_l") { newValue ->
            if (newValue is String && newValue.toDoubleOrNull() != null) {
                applyProp(RKBDProp.BottomCorners, newValue.toDouble())
            }
        }
    }

    @SuppressLint("SdCardPath")
    private fun getCurrentXmlValues(file: RKBDFile): Map<String, Any> {

        val output = HashMap<String, Any>()

        val fileName = "/data/data/${Config.GBOARD_PACKAGE_NAME}/shared_prefs/${file.rawValue}"
        val xmlFile = SuFile(fileName)
        if (!xmlFile.exists()) return output

        val map = try {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                InputSource(
                    StringReader(
                        SuFileInputStream.open(xmlFile).bufferedReader().readText()
                    )
                )
            ).getElementsByTagName("map")
        } catch (e: Exception) {
            return output
        }

        for (item in map.item(0).childNodes) {
            val name = item.attributes?.getNamedItem("name")?.nodeValue
            val value = item.attributes?.getNamedItem("value")?.nodeValue
            if (name != null) output[name] =
                (value?.booleanOrNull() ?: value) ?: item.textContent ?: ""
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
                            setDefaultValue(default)
                            setSummary(default)
                        }
                        is EditTextPreference -> {
                            val default = (defaultValues[flag.rawValue] ?: "") as String
                            setDefaultValue(default)
                            setSummary(default)
                        }
                        is CustomDialogPreference -> {
                            val default = (defaultValues[flag.rawValue] ?: "0.0") as String
                            setDefaultValue((default.toDouble() * 100).toInt())
                            summary = default
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
                            val default = sharedPreferences.getInt("${key}_pref", 95)
                            setDefaultValue((default.toDouble() / 100).toString())
                            setSummary("${default.toDouble() / 100}")
                        }
                        is EditTextPreference -> {
                            val default = sharedPreferences.getString("${key}_pref", "")
                            setDefaultValue(default)
                            setSummary("$default")
                        }
                        is CustomDialogPreference -> {
                            val default = sharedPreferences.getInt("${key}_pref", 95)
                            setDefaultValue(default)
                            setSummary(default / 100)
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
                                        val value: Double = (newValue.toDouble() / 10)
                                        setSummary("$value")
                                    }
                                }
                                is EditTextPreference -> {
                                    if (newValue is String) {
                                        setSummary(newValue)
                                    }
                                }
                                is CustomDialogPreference -> {
                                    if (newValue is Number) {
                                        summary = (newValue.toDouble() / 100).toString()
                                    }
                                }
                                else -> {
                                    summary = newValue.toString()
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