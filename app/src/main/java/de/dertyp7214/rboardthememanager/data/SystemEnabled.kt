package de.dertyp7214.rboardthememanager.data

import android.view.ViewGroup
import android.widget.TextView
import de.dertyp7214.rboardthememanager.R

class SystemEnabled(val enabled: Boolean, systemView: ViewGroup) {
    init {
        systemView.findViewById<TextView>(R.id.systemTitle).setTextColor(
            if (enabled) systemView.resources.getColor(R.color.colorAccent, null)
            else systemView.resources.getColor(R.color.disabledText, null)
        )
        systemView.findViewById<TextView>(R.id.systemSummary).setTextColor(
            if (enabled) systemView.resources.getColor(R.color.primaryText, null)
            else systemView.resources.getColor(R.color.disabledText, null)
        )
    }
}