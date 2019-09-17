package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.dertyp7214.rboardthememanager.R

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String): WelcomeFragment {
            return WelcomeFragment()
        }
    }
}
