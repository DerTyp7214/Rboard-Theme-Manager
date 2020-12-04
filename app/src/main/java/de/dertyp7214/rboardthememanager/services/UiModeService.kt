package de.dertyp7214.rboardthememanager.services

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.dertyp7214.logs.helpers.Logger
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.helper.ThemeHelper
import de.dertyp7214.rboardthememanager.screens.SplashScreen

class UiModeService : Service() {

    companion object {
        var RUNNING = false
            private set
        private var THREAD: Thread? = null
        private var LAST_STATE: Int? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification =
            NotificationCompat.Builder(this, getString(R.string.ui_mode_service_channel))
                .setContentTitle(getString(R.string.ui_mode_service_title))
                .setContentText(getString(R.string.ui_mode_service_text))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, SplashScreen::class.java).apply {
                            putExtra(
                                "action",
                                "close"
                            )
                        },
                        0
                    )
                )
                .build()
        startForeground(1234, notification)
        if (!RUNNING) {
            Logger.log(
                Logger.Companion.Type.DEBUG,
                "UiModeService",
                "[UiModeService]: started!",
                applicationContext
            )
            RUNNING = true
            THREAD = Thread {
                Looper.prepare()
                fun loop() {
                    val currentState =
                        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    if (LAST_STATE == null) LAST_STATE = currentState
                    else if (currentState != LAST_STATE) {
                        LAST_STATE = currentState
                        val dark = getSharedPreferences("darkTheme", MODE_PRIVATE).let {
                            Pair(
                                it.getString(
                                    "name",
                                    null
                                ), it.getBoolean("border", false)
                            )
                        }
                        val light = getSharedPreferences("lightTheme", MODE_PRIVATE).let {
                            Pair(
                                it.getString(
                                    "name",
                                    null
                                ), it.getBoolean("border", false)
                            )
                        }
                        var name: String? = null
                        var border = false
                        when (currentState) {
                            Configuration.UI_MODE_NIGHT_NO -> {
                                Logger.log(
                                    Logger.Companion.Type.DEBUG,
                                    "UiModeService",
                                    "[UiModeService]: Light Mode!",
                                    applicationContext
                                )
                                name = light.first
                                border = light.second
                            }
                            Configuration.UI_MODE_NIGHT_YES -> {
                                Logger.log(
                                    Logger.Companion.Type.DEBUG,
                                    "UiModeService",
                                    "[UiModeService]: Dark Mode!",
                                    applicationContext
                                )
                                name = dark.first
                                border = dark.second
                            }
                        }
                        Logger.log(
                            Logger.Companion.Type.DEBUG,
                            "YEET",
                            "$name $border"
                        )
                        if (name != null && !(dark.first == light.first && dark.second == light.second)) ThemeHelper.applyTheme(
                            "$name.zip",
                            border,
                            applicationContext
                        )
                    }
                    Thread.sleep(1000)
                    if (THREAD?.isInterrupted == false) loop()
                }
                try {
                    loop()
                } catch (e: InterruptedException) {
                    RUNNING = false
                    Logger.log(
                        Logger.Companion.Type.DEBUG,
                        "UiModeService",
                        "[UiModeService]: thread interrupted!",
                        applicationContext
                    )
                }
            }.apply { start() }
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        RUNNING = false
        Logger.log(
            Logger.Companion.Type.DEBUG,
            "UiModeService",
            "[UiModeService]: destroyed!",
            applicationContext
        )
        THREAD?.interrupt()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    class BootBroadcastReceiver : BroadcastReceiver() {
        companion object {
            private const val ACTION = Intent.ACTION_BOOT_COMPLETED
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION && context != null && !RUNNING && false) {
                Logger.context = context.applicationContext
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    context.startForegroundService(
                        Intent(
                            context.applicationContext,
                            UiModeService::class.java
                        )
                    )
                else context.startService(
                    Intent(
                        context.applicationContext,
                        UiModeService::class.java
                    )
                )
            }
        }
    }
}