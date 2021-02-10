package de.dertyp7214.rboardthememanager.screens

import android.Manifest
import android.app.WallpaperManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.get
import androidx.navigation.fragment.findNavController
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.core.getBitmap
import kotlinx.android.synthetic.main.activity_authentication.*

class AuthenticationActivity : AppCompatActivity() {

    private var back = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            42069
        )
    }

    override fun onBackPressed() {
        val controller = authNav.findNavController()
        when (controller.currentDestination?.label ?: "") {
            getString(R.string.label_nav_auth_main) -> {
                back++
                if (back < 2)
                    Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show()
                else finishAffinity()
                Thread {
                    Thread.sleep(5000)
                    back--
                    if (back < 0) back = 0
                }.start()
            }
            getString(R.string.label_nav_auth_login) -> controller.navigate(R.id.action_authLoginFragment_to_authMainFragment)
            getString(R.string.label_nav_auth_success) -> controller.navigate(R.id.action_authSuccessFragment_to_authMainFragment)
            getString(R.string.label_nav_auth_failed) -> controller.navigate(R.id.action_authFailedFragment_to_authMainFragment)
            else -> super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            42069 -> {
                WallpaperManager.getInstance(this).drawable?.apply {
                    findViewById<ImageView>(R.id.background).setImageDrawable(this)
                    window.statusBarColor =
                        ColorUtils.blendARGB(
                            getColor(R.color.colorPrimary),
                            getBitmap().let { it[it.width / 2, 0] },
                            if (resources.getBoolean(R.bool.darkmode)) .1F else .03F
                        )
                    window.navigationBarColor =
                        ColorUtils.blendARGB(
                            getColor(R.color.colorPrimary),
                            getBitmap().let { it[it.width / 2, it.height - 1] },
                            if (resources.getBoolean(R.bool.darkmode)) .1F else .03F
                        )
                }
                return
            }
        }
    }
}
