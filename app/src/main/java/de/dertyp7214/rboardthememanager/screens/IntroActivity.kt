package de.dertyp7214.rboardthememanager.screens

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.fragments.SelectRuntimeData
import de.dertyp7214.rboardthememanager.viewmodels.IntroViewModel
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.intro_navigator.*

class IntroActivity : AppCompatActivity() {

    var index: Int = 0
    lateinit var introViewModel: IntroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(de.dertyp7214.rboardthememanager.R.layout.activity_intro)

        introViewModel = ViewModelProviders.of(this)[IntroViewModel::class.java]
        introViewModel.selectRuntimeData.value = SelectRuntimeData()
        introViewModel.setRboardPermission(
            ContextCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )

        indicator.createIndicators(3, index)

        floatingActionButton.setOnClickListener {
            index++
            openPage()
        }
    }

    private fun openPage() {
        val controller = fragment.findNavController()
        val name = resources.getResourceEntryName(controller.currentDestination?.id ?: 0)
        when (index) {
            0 -> {
                controller.navigate(R.id.action_selectRuntimeFragment_to_welcomeFragment)
            }
            1 -> {
                if (name == "welcomeFragment") controller.navigate(R.id.action_welcomeFragment_to_selectRuntimeFragment)
                else controller.navigate(R.id.action_permissionsFragment_to_selectRuntimeFragment)
            }
            2 -> {
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
                    introViewModel.setRboardPermission(true)
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
            perm != null
        } ?: false
    }
}
