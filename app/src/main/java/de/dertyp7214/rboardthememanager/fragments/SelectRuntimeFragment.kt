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
import de.dertyp7214.rboardthememanager.Config.MODULE_ID
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.utils.MagiskUtils
import de.dertyp7214.rboardthememanager.viewmodels.IntroViewModel

class SelectRuntimeFragment : Fragment() {

    private lateinit var introViewModel: IntroViewModel
    private lateinit var ac: FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ac = activity!!

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

        val magiskInstalled = MagiskUtils.isMagiskInstalled()
        if (introViewModel.selected.value != true) {
            MagiskUtils.getModules().any { it.id == MODULE_ID }.apply {
                introViewModel.setMagisk(this)
                introViewModel.setSystem(!this)
            }
            introViewModel.selected.value = true
        }

        val textMagisk = v.findViewById<TextView>(R.id.magiskText)
        val clickMagisk = v.findViewById<LinearLayout>(R.id.clickMagisk)
        val radioMagisk = v.findViewById<RadioButton>(R.id.radioMagisk)
        val clickSystem = v.findViewById<LinearLayout>(R.id.clickSystem)
        val radioSystem = v.findViewById<RadioButton>(R.id.radioSystem)

        clickMagisk.setOnClickListener {
            if (magiskInstalled) {
                radioMagisk.isChecked = true
                radioSystem.isChecked = false
                introViewModel.setMagisk(true)
                introViewModel.setSystem(false)
            }
        }

        clickSystem.setOnClickListener {
            radioSystem.isChecked = true
            radioMagisk.isChecked = false
            introViewModel.setSystem(true)
            introViewModel.setMagisk(false)
        }

        textMagisk.setTextColor(
            if (magiskInstalled) resources.getColor(
                R.color.colorAccent,
                null
            ) else Color.LTGRAY
        )
        radioMagisk.isEnabled = magiskInstalled
        radioMagisk.isChecked = introViewModel.selectRuntimeData.value?.magisk ?: false
        radioSystem.isChecked = introViewModel.selectRuntimeData.value?.system ?: true

        return v
    }
}

data class SelectRuntimeData(var system: Boolean = true, var magisk: Boolean = false)