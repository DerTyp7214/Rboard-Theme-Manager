package de.dertyp7214.rboardthememanager

import android.app.Application
import com.dertyp7214.logs.helpers.Logger

class Application: Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.init(this)
    }
}