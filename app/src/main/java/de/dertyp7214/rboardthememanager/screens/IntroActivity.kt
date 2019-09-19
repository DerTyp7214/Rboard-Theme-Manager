package de.dertyp7214.rboardthememanager.screens

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.fragments.SelectRuntimeData
import de.dertyp7214.rboardthememanager.viewmodels.IntroViewModel
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.intro_navigator.*

class IntroActivity : AppCompatActivity() {

    private var index: Int = 0
    private lateinit var introViewModel: IntroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        introViewModel = ViewModelProviders.of(this)[IntroViewModel::class.java]
        introViewModel.selectRuntimeData.value = SelectRuntimeData()
        introViewModel.setRboardPermission(
            ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )

        indicator.createIndicators(3, index)
        fragment.findNavController().navigate(R.id.welcomeFragment)

        floatingActionButton.setOnClickListener {
            index++
            openPage()
        }

        introViewModel.rboardStorage.observe(this, Observer {
            val controller = fragment.findNavController()
            val name = resources.getResourceEntryName(controller.currentDestination?.id ?: 0)
            if (name == "permissionsFragment") {
                floatingActionButton.isEnabled = it && introViewModel.gboardPermission()
                if (it) floatingActionButton.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.colorAccent))
                else floatingActionButton.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            }
        })

        introViewModel.gboardStorage.observe(this, Observer {
            val controller = fragment.findNavController()
            val name = resources.getResourceEntryName(controller.currentDestination?.id ?: 0)
            if (name == "permissionsFragment") {
                floatingActionButton.isEnabled = it && introViewModel.rboardPermission()
                if (it) floatingActionButton.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.colorAccent))
                else floatingActionButton.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            }
        })
    }

    private fun openPage() {
        val controller = fragment.findNavController()
        val name = resources.getResourceEntryName(controller.currentDestination?.id ?: 0)
        floatingActionButton.isEnabled = true
        floatingActionButton.backgroundTintList =
            ColorStateList.valueOf(getColor(R.color.colorAccent))
        when (index) {
            0 -> {
                controller.navigate(R.id.action_selectRuntimeFragment_to_welcomeFragment)
            }
            1 -> {
                if (name == "welcomeFragment") controller.navigate(R.id.action_welcomeFragment_to_selectRuntimeFragment)
                else controller.navigate(R.id.action_permissionsFragment_to_selectRuntimeFragment)
            }
            2 -> {
                if (!introViewModel.rboardPermission() || !introViewModel.gboardPermission()) {
                    floatingActionButton.isEnabled = false
                    floatingActionButton.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
                }
                if (name == "selectRuntimeFragment") controller.navigate(R.id.action_selectRuntimeFragment_to_permissionsFragment)
                else {
                    index--
                    openPage()
                }
            }
        }
        if (index < 3) {
            indicator.animatePageSelected(index)
        } else {
            index = 3
            startActivity(Intent(this, HomeActivity::class.java))
            getSharedPreferences("start", Context.MODE_PRIVATE).apply {
                edit {
                    putBoolean("first", false)
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (index != 0) {
            index--
            openPage()
        }
    }

    override fun onResume() {
        super.onResume()
        introViewModel.setGboardPermission(checkGboardPermission())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    introViewModel.rboardStorage.postValue(true)
                } else {
                    Toast.makeText(
                        this,
                        "Permission denied to read your External storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    private fun checkGboardPermission(): Boolean { // TODO: does not work
        return packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS).find {
            it.packageName == "com.google.android.inputmethod.latin"
        }?.let {
            val perm = it.requestedPermissions?.filterIndexed { index, p ->
                p == "android.permission.READ_EXTERNAL_STORAGE" && ((it.requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0)
            }
            perm != null && perm.contains("android.permission.READ_EXTERNAL_STORAGE")
        } ?: false
    }
}
