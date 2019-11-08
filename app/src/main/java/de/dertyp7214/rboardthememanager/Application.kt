package de.dertyp7214.rboardthememanager

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.dertyp7214.logs.helpers.Logger

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            AppCompatDelegate.setDefaultNightMode(
                getInt(
                    "theme_pref",
                    AppCompatDelegate.getDefaultNightMode()
                )
            )
        }
    }
}