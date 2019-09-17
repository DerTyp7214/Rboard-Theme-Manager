package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import de.dertyp7214.rboardthememanager.R

class SelectRuntimeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_select_runtime, container, false)

        val clickMagisk = v.findViewById<LinearLayout>(R.id.clickMagisk)
        val radioMagik = v.findViewById<RadioButton>(R.id.radioMagisk)
        val clickSystem = v.findViewById<LinearLayout>(R.id.clickSystem)
        val radioSystem = v.findViewById<RadioButton>(R.id.radioSystem)

        clickMagisk.setOnClickListener {
            radioMagik.isChecked = true
            radioSystem.isChecked = false
        }

        clickSystem.setOnClickListener {
            radioSystem.isChecked = true
            radioMagik.isChecked = false
        }

        return v
    }
}

data class SelectRuntimeData(var system: Boolean = true, var magisk: Boolean = false)