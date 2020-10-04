package de.dertyp7214.rboardthememanager.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.topjohnwu.superuser.Shell
import de.dertyp7214.rboardthememanager.Config.MODULE_ID
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.data.SystemEnabled
import de.dertyp7214.rboardthememanager.databinding.FragmentSelectRuntimeBinding
import de.dertyp7214.rboardthememanager.utils.MagiskUtils
import de.dertyp7214.rboardthememanager.viewmodels.IntroViewModel

class SelectRuntimeFragment : Fragment() {

    private lateinit var introViewModel: IntroViewModel
    private lateinit var ac: FragmentActivity
    private lateinit var binding: FragmentSelectRuntimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ac = requireActivity()

        introViewModel = ac.run {
            ViewModelProviders.of(this)[IntroViewModel::class.java]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_select_runtime, container, false)
        binding = FragmentSelectRuntimeBinding.bind(v)

        val magiskInstalled = MagiskUtils.isMagiskInstalled()

        val textMagisk = v.findViewById<TextView>(R.id.magiskText)
        val clickMagisk = v.findViewById<LinearLayout>(R.id.clickMagisk)
        val radioMagisk = v.findViewById<RadioButton>(R.id.radioMagisk)
        val clickSystem = v.findViewById<LinearLayout>(R.id.clickSystem)
        val radioSystem = v.findViewById<RadioButton>(R.id.radioSystem)
        val magiskInstalledText = v.findViewById<TextView>(R.id.magisk_installed)

        if (introViewModel.selected.value != true) {
            if (introViewModel.magiskInstalled.value != true) introViewModel.magiskInstalled.value =
                Shell.rootAccess()
            MagiskUtils.getModules().any { it.id == MODULE_ID }.apply {
                introViewModel.setMagisk(this)
                introViewModel.setSystem(!this)
            }
            introViewModel.selected.value = true
        }

        clickMagisk.setOnClickListener {
            if (magiskInstalled) {
                radioMagisk.isChecked = true
                radioSystem.isChecked = false
                introViewModel.setMagisk(true)
                introViewModel.setSystem(false)
            }
        }

        binding.systemEnabled = SystemEnabled(!magiskInstalled, clickSystem)
        if (!magiskInstalled) {
            clickSystem.setOnClickListener {
                radioSystem.isChecked = true
                radioMagisk.isChecked = false
                introViewModel.setSystem(true)
                introViewModel.setMagisk(false)
            }
        } else clickSystem.setOnClickListener(null)

        textMagisk.setTextColor(
            if (magiskInstalled) resources.getColor(
                R.color.colorAccent,
                null
            ) else Color.LTGRAY
        )
        radioMagisk.isEnabled = magiskInstalled
        radioMagisk.isChecked = introViewModel.selectRuntimeData.value?.magisk ?: false
        radioSystem.isChecked = introViewModel.selectRuntimeData.value?.system ?: true

        magiskInstalledText.visibility =
            if (introViewModel.magiskInstalled.value == true) View.GONE else View.VISIBLE

        return v
    }
}

data class SelectRuntimeData(var system: Boolean = true, var magisk: Boolean = false)