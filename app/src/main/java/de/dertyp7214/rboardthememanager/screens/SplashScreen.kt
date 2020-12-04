package de.dertyp7214.rboardthememanager.screens

import android.Manifest
import android.animation.ObjectAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.topjohnwu.superuser.BusyBoxInstaller
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.appupdater.core.checkUpdate
import de.dertyp7214.rboardthememanager.BuildConfig
import de.dertyp7214.rboardthememanager.Config
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.component.NoRootBottomSheet
import de.dertyp7214.rboardthememanager.core.runAsCommand
import de.dertyp7214.rboardthememanager.utils.FileUtils
import de.dertyp7214.rboardthememanager.utils.ThemeUtils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class SplashScreen : AppCompatActivity() {

    private var close = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Shell.Config.setFlags(Shell.FLAG_MOUNT_MASTER)
        Shell.Config.verboseLogging(BuildConfig.DEBUG)

        Shell.setDefaultBuilder(Shell.Builder.create().apply {
            setFlags(Shell.FLAG_MOUNT_MASTER)
            setInitializers(BusyBoxInstaller::class.java)
        })

        intent.extras?.apply {
            getString("action")?.apply {
                when (this) {
                    "close" -> close = true
                }
            }
        }

        createNotificationChannel()
        FirebaseMessaging.getInstance()
            .subscribeToTopic("update-${BuildConfig.BUILD_TYPE.toLowerCase(Locale.ROOT)}")

        "rm -rf ${cacheDir.absolutePath}/*".runAsCommand()
        val files = ArrayList<File>()
        files += FileUtils.getThemePacksPath(this).listFiles()!!
        files += FileUtils.getSoundPacksPath(this).listFiles()!!
        files.forEach {
            SuFile(it.absolutePath).deleteRecursive()
        }

        if (Shell.rootAccess()) {
            if (!checkGboardPermission()) requestGboardStorage()
            if (ThemeUtils.checkForExistingThemes()) ThemeUtils.getThemesPathFromProps()
                ?.apply { Config.THEME_LOCATION = this }

            //ThemeUtils.changeThemesPath("/data/GboardThemes")

            getSharedPreferences("auth", Context.MODE_PRIVATE).apply {
                //if (getBoolean("registered", false)) {
                getSharedPreferences("start", Context.MODE_PRIVATE).apply {
                    if (getBoolean("first", true) || ContextCompat.checkSelfPermission(
                            this@SplashScreen,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) runAnimation()
                    else {
                        checkUpdate {
                            startApp()
                        }
                    }
                }
                //} else {
                //    startActivity(Intent(this@SplashScreen, AuthenticationActivity::class.java))
                //}
            }
        } else NoRootBottomSheet().show(supportFragmentManager, "YEET")
    }

    private fun checkUpdate(callback: () -> Unit) {
        checkUpdate(
            clazz = null,
            timeout = 1000,
            updateUrl = "https://api.dertyp7214.de/${
                BuildConfig.BUILD_TYPE.toLowerCase(
                    Locale.ROOT
                )
            }",
            versionCode = BuildConfig.VERSION_CODE,
            forceUpdate = false
        ) {
            runOnUiThread {
                callback()
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
            checkUpdate {
                if (!close) startActivity(Intent(this, IntroActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }

    private fun startApp() {
        if (!close) startActivity(Intent(this, HomeActivity::class.java))
        finish()
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

            val name2 = getString(R.string.channel_name_ui_mode)
            val channelId2 = getString(R.string.ui_mode_service_channel)
            val descriptionText2 = getString(R.string.channel_description_ui_mode)
            val importance2 = NotificationManager.IMPORTANCE_NONE
            val channel2 = NotificationChannel(channelId2, name2, importance2).apply {
                description = descriptionText2
            }
            notificationManager.createNotificationChannel(channel2)
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
            perm != null && perm.contains("android.permission.READ_EXTERNAL_STORAGE")
        } ?: false
    }

    private fun requestGboardStorage() {
        "pm grant ${Config.GBOARD_PACKAGE_NAME} android.permission.READ_EXTERNAL_STORAGE".runAsCommand()
    }
}