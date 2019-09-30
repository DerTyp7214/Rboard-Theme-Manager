package de.dertyp7214.rboardthememanager.notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.screens.SplashScreen

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class Service : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        when (remoteMessage.from?.replace("/topics/", "")) {
            "update-release" -> sendUpdateNotification(remoteMessage)
            "update-debug" -> sendUpdateNotification(remoteMessage)
        }
    }

    private fun sendUpdateNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, SplashScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notificationBuilder =
            NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setContentTitle(remoteMessage.notification?.title)
                .setContentText(remoteMessage.notification?.body)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}