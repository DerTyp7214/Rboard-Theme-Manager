package de.dertyp7214.rboardthememanager.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import de.dertyp7214.rboardthememanager.R
import kotlin.system.exitProcess

class NoRootBottomSheet :
    RoundedBottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.no_root_bottomsheet, container, false)

        v.findViewById<Button>(R.id.button_close).setOnClickListener {
            requireActivity().apply {
                exitProcess(0)
            }
        }

        return v
    }
}