package de.dertyp7214.rboardthememanager.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.viewmodels.IntroViewModel

class PermissionsFragment : Fragment() {

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
        val v = inflater.inflate(R.layout.fragment_permissions, container, false)

        val rboardPermissionBox = v.findViewById<LinearLayout>(R.id.clickRboardStorage)
        val gboardPermissionBox = v.findViewById<LinearLayout>(R.id.clickGboardStorage)
        val checkBoxRboard = v.findViewById<CheckBox>(R.id.radioRboard)
        val checkBoxGboard = v.findViewById<CheckBox>(R.id.radioGboard)

        rboardPermissionBox.setOnClickListener {
            ActivityCompat.requestPermissions(
                ac,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }

        gboardPermissionBox.setOnClickListener {
            requestGboardStorage()
        }

        introViewModel.rboardStorage.observe(this, Observer {
            checkBoxRboard.isChecked = it
        })

        introViewModel.gboardStorage.observe(this, Observer {
            checkBoxGboard.isChecked = it
        })

        checkBoxRboard.isChecked = introViewModel.rboardPermission()
        checkBoxGboard.isChecked = introViewModel.gboardPermission()

        return v
    }

    private fun requestGboardStorage() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", "com.google.android.inputmethod.latin", null)
        intent.data = uri
        startActivity(intent)
    }
}
