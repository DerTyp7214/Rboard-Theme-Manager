package de.dertyp7214.rboardthememanager.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.screens.SplashScreen

class AuthSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_auth_success, container, false)

        val activity = requireActivity()

        activity.getSharedPreferences("auth", Context.MODE_PRIVATE)?.edit {
            putBoolean("registered", true)
        }

        val buttonNext = v.findViewById<Button>(R.id.btn_next)

        buttonNext.setOnClickListener {
            activity.apply { startActivity(Intent(this, SplashScreen::class.java)) }
        }

        return v
    }
}
