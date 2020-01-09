package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.disable
import de.dertyp7214.rboardthememanager.core.enable
import kotlinx.android.synthetic.main.activity_authentication.*

class AuthMainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_auth_main, container, false)

        val controller = authNav.findNavController()

        val buttonRegister = v.findViewById<Button>(R.id.btn_register)
        val buttonLogin = v.findViewById<Button>(R.id.btn_login)

        buttonLogin.setOnClickListener {
            controller.navigate(R.id.action_authMainFragment_to_authLoginFragment)
        }

        buttonRegister.setOnClickListener {
            val accent = resources.getColor(R.color.colorAccent, null).apply {
                buttonRegister.disable(this)
                buttonLogin.disable(this)
            }
            // TODO: backend to register
            Handler().postDelayed({
                controller.navigate(R.id.action_authMainFragment_to_authSuccessFragment)
                buttonRegister.enable(accent)
                buttonLogin.enable(accent)
            }, 2000)
        }

        return v
    }
}
