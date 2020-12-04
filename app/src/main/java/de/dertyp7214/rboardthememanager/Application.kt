package de.dertyp7214.rboardthememanager

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.Shell
import de.dertyp7214.rboardthememanager.services.UiModeService

class Application : Application() {

    companion object {
        var context: Context? = null
            private set
        var SHELL: Shell? = null
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
            if (getBoolean("service_key", false)) {
                if (UiModeService.RUNNING) stopService(
                    Intent(
                        this@Application,
                        UiModeService::class.java
                    )
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(
                        Intent(
                            this@Application,
                            UiModeService::class.java
                        )
                    )
                else startService(
                    Intent(
                        this@Application,
                        UiModeService::class.java
                    )
                )
            }
        }
    }
}