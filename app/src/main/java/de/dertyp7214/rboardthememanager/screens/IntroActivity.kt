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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.button.MaterialButton
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.Config.MODULE_ID
import de.dertyp7214.rboardthememanager.Config.THEME_LOCATION
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.runAsCommand
import de.dertyp7214.rboardthememanager.data.ModuleMeta
import de.dertyp7214.rboardthememanager.fragments.SelectRuntimeData
import de.dertyp7214.rboardthememanager.utils.MagiskUtils
import de.dertyp7214.rboardthememanager.viewmodels.IntroViewModel
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.intro_navigator.*
import kotlin.system.exitProcess

class IntroActivity : AppCompatActivity() {

    private var index: Int = 0
    private lateinit var introViewModel: IntroViewModel
    private var colorDisabled = Color.LTGRAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        colorDisabled = if (resources.getBoolean(R.bool.darkmode)) Color.DKGRAY else Color.LTGRAY

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

        introViewModel.rboardStorage.observe(this, {
            val controller = fragment.findNavController()
            val name = resources.getResourceEntryName(controller.currentDestination?.id ?: 0)
            if (name == "permissionsFragment") {
                floatingActionButton.isEnabled = it && introViewModel.gboardPermission()
                if (it) floatingActionButton.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.colorAccent))
                else floatingActionButton.backgroundTintList = ColorStateList.valueOf(colorDisabled)
            }
        })

        introViewModel.gboardStorage.observe(this, {
            val controller = fragment.findNavController()
            val name = resources.getResourceEntryName(controller.currentDestination?.id ?: 0)
            if (name == "permissionsFragment") {
                floatingActionButton.isEnabled = it && introViewModel.rboardPermission()
                if (it) floatingActionButton.backgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.colorAccent))
                else floatingActionButton.backgroundTintList = ColorStateList.valueOf(colorDisabled)
            }
        })

        introViewModel.magiskInstalled.observe(this, {
            floatingActionButton.isEnabled = it
            floatingActionButton.backgroundTintList =
                ColorStateList.valueOf(if (it) getColor(R.color.colorAccent) else colorDisabled)
        })

        skipIntro()
    }

    private fun skipIntro() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE),
            1234
        )
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
                if (introViewModel.magiskInstalled.value != true) {
                    floatingActionButton.isEnabled = false
                    floatingActionButton.backgroundTintList = ColorStateList.valueOf(colorDisabled)
                }
                if (name == "welcomeFragment") controller.navigate(R.id.action_welcomeFragment_to_selectRuntimeFragment)
                else controller.navigate(R.id.action_permissionsFragment_to_selectRuntimeFragment)
            }
            2 -> {
                if (!introViewModel.rboardPermission() || !introViewModel.gboardPermission()) {
                    floatingActionButton.isEnabled = false
                    floatingActionButton.backgroundTintList = ColorStateList.valueOf(colorDisabled)
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
            startApp()
        }
    }

    private fun startApp() {
        //if (introViewModel.selectRuntimeData.value?.magisk == true) {
        if (!MagiskUtils.getModules().any { it.id == MODULE_ID }) {
            val meta = ModuleMeta(
                MODULE_ID,
                "Rboard Themes",
                "v20",
                "200",
                "RKBDI & DerTyp7214",
                "Module for Rboard Themes app"
            )
            val file = mapOf(
                Pair(
                    "system.prop",
                    "ro.com.google.ime.theme_file=veu.zip\nro.com.google.ime.themes_dir=$THEME_LOCATION"
                ),
                Pair(THEME_LOCATION, null)
            )
            MagiskUtils.installModule(meta, file)
            MaterialDialog(this).show {
                setContentView(R.layout.reboot_dialog)
                findViewById<MaterialButton>(R.id.button_later).setOnClickListener { exitProcess(0) }
                findViewById<MaterialButton>(R.id.button_restart).setOnClickListener {
                    "reboot".runAsCommand()
                }
            }
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        //}
        getSharedPreferences("start", Context.MODE_PRIVATE).apply {
            edit {
                putBoolean("first", false)
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
            1234 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startApp()
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

    private fun checkGboardPermission(): Boolean {
        return packageManager.getPackageInfo(
            Config.GBOARD_PACKAGE_NAME,
            PackageManager.GET_PERMISSIONS
        )?.let {
            val perm = it.requestedPermissions?.filterIndexed { index, p ->
                p == "android.permission.READ_EXTERNAL_STORAGE" && ((it.requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0)
            }
            perm != null && "android.permission.READ_EXTERNAL_STORAGE" in perm
        } ?: false
    }
}
