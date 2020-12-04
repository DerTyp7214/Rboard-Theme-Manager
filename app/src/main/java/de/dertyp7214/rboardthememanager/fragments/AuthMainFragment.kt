package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.auth.Api
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
        val keyInput = v.findViewById<TextInputEditText>(R.id.key_input)
        val emailInput = v.findViewById<TextInputEditText>(R.id.email_input)
        val keyBox = v.findViewById<TextInputLayout>(R.id.key_parent)
        val emailBox = v.findViewById<TextInputLayout>(R.id.email_parent)

        val activity = requireActivity()

        v.findViewById<ViewGroup>(R.id.root).setOnClickListener {
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

        buttonLogin.setOnClickListener {
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            controller.navigate(R.id.action_authMainFragment_to_authLoginFragment)
        }

        emailBox.setOnClickListener { emailBox.isErrorEnabled = false }
        emailInput.setOnClickListener { emailBox.isErrorEnabled = false }
        emailInput.doOnTextChanged { _, _, _, _ -> emailBox.isErrorEnabled = false }
        keyBox.setOnClickListener { keyBox.isErrorEnabled = false }
        keyInput.setOnClickListener { keyBox.isErrorEnabled = false }
        keyInput.doOnTextChanged { _, _, _, _ -> keyBox.isErrorEnabled = false }

        buttonRegister.setOnClickListener {
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if (emailInput.text.isNullOrEmpty() || emailInput.text.isNullOrBlank() || keyInput.text.isNullOrEmpty() || keyInput.text.isNullOrBlank()) {
                if (emailInput.text.isNullOrEmpty() || emailInput.text.isNullOrBlank()) {
                    emailBox.error = getString(R.string.inputError)
                    emailBox.isErrorEnabled = true
                }
                if (keyInput.text.isNullOrEmpty() || keyInput.text.isNullOrBlank()) {
                    keyBox.error = getString(R.string.inputError)
                    keyBox.isErrorEnabled = true
                }
            } else {
                val accent = resources.getColor(R.color.colorAccent, null).apply {
                    buttonRegister.disable(this)
                    buttonLogin.disable(this)
                }
                Thread {
                    val response =
                        Api.register(keyInput.text.toString(), emailInput.text.toString())
                    activity.runOnUiThread {
                        if (response) controller.navigate(R.id.action_authMainFragment_to_authSuccessFragment)
                        else controller.navigate(R.id.action_authMainFragment_to_authFailedFragment)
                        buttonRegister.enable(accent)
                        buttonLogin.enable(accent)
                    }
                }.start()
            }
        }

        return v
    }
}
