package de.dertyp7214.rboardthememanager.screens

import android.Manifest
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.topjohnwu.superuser.Shell
import de.dertyp7214.appupdater.core.checkUpdate
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.R
import java.util.*

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Shell.Config.setFlags(Shell.FLAG_USE_MAGISK_BUSYBOX and Shell.FLAG_REDIRECT_STDERR)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)

        createNotificationChannel()
        FirebaseMessaging.getInstance()
            .subscribeToTopic("update-${BuildConfig.BUILD_TYPE.toLowerCase(Locale.ROOT)}")

        checkUpdate(
            clazz = null,
            updateUrl = "https://api.dertyp7214.de/${BuildConfig.BUILD_TYPE.toLowerCase(
                Locale.ROOT
            )}", versionCode = BuildConfig.VERSION_CODE, forceUpdate = false
        ) {
            runOnUiThread {
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val channelId = getString(R.string.default_notification_channel_id)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
