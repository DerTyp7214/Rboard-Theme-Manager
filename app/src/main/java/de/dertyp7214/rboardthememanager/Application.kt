package de.dertyp7214.rboardthememanager

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.dertyp7214.logs.helpers.Logger

class Application : Application() {

    companion object {
        var context: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        context = this
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            AppCompatDelegate.setDefaultNightMode(
                getInt(
                    "theme_pref",
                    AppCompatDelegate.getDefaultNightMode()
                )
            )
        }

        /*if (UiModeService.RUNNING) stopService(Intent(this, UiModeService::class.java))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(
                Intent(
                    this,
                    UiModeService::class.java
                )
            )
        else startService(
            Intent(
                this,
                UiModeService::class.java
            )
        )*/
    }
}