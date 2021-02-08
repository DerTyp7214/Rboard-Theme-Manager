package de.dertyp7214.rboardthememanager

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.dertyp7214.logs.helpers.Logger
import com.topjohnwu.superuser.Shell

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
        }
    }
}