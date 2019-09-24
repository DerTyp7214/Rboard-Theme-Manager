package de.dertyp7214.rboardthememanager.screens

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import de.dertyp7214.appupdater.core.checkUpdate
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.R
import java.util.*

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        checkUpdate(
            updateUrl = "https://api.dertyp7214.de/${BuildConfig.BUILD_TYPE.toLowerCase(
                Locale.ROOT
            )}", versionCode = BuildConfig.VERSION_CODE, forceUpdate = false
        ) {
            getSharedPreferences("start", Context.MODE_PRIVATE).apply {
                if (getBoolean("first", true) || ContextCompat.checkSelfPermission(
                        this@SplashScreen,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) runAnimation()
                else startApp()
            }
        }
    }

    private fun runAnimation() {
        val image = findViewById<ImageView>(R.id.image)
        ObjectAnimator.ofFloat(image, "scaleY", 1f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(image, "scaleX", 1f).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }.doOnEnd {
            startActivity(Intent(this, IntroActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun startApp() {
        startActivity(Intent(this, HomeActivity::class.java))
    }
}
