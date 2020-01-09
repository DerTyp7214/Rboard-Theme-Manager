package de.dertyp7214.rboardthememanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.dertyp7214.rboardthememanager.R
import kotlinx.android.synthetic.main.activity_authentication.*

class AuthFailedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_auth_failed, container, false)

        val buttonRetry = v.findViewById<Button>(R.id.btn_retry)

        buttonRetry.setOnClickListener {
            authNav.findNavController().navigate(R.id.action_authFailedFragment_to_authMainFragment)
        }

        return v
    }
}
