package de.dertyp7214.rboardthememanager

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.dertyp7214.logs.helpers.Logger

class Application : Application() {

    companion object {
        var context: Application? = null
            private set

        var uiHandler: Handler? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
        context = this
        uiHandler = Handler(Looper.getMainLooper())
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