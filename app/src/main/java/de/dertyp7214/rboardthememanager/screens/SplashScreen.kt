package de.dertyp7214.rboardthememanager.screens

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.R

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        getSharedPreferences("start", Context.MODE_PRIVATE).apply {
            if (getBoolean("first", true)) runAnimation()
            else startApp()
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
        }
    }

    private fun startApp() {
        startActivity(Intent(this, HomeActivity::class.java))
    }
}
