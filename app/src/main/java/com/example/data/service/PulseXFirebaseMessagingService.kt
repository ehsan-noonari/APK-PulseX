package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PulseXFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
            }
        } catch (e: Exception) {
            Log.e("FCM", "FCM onNewToken database update failed: ${e.message}")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            handleNotification(
                title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "PulseX News",
                body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "",
                articleId = remoteMessage.data["articleId"]
            )
        } else if (remoteMessage.notification != null) {
            Log.d("FCM", "Message Notification Body: ${remoteMessage.notification?.body}")
            handleNotification(
                title = remoteMessage.notification?.title ?: "PulseX News",
                body = remoteMessage.notification?.body ?: "",
                articleId = null
            )
        }
    }

    private fun handleNotification(title: String, body: String, articleId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (articleId != null) {
                putExtra("articleId", articleId)
            }
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "pulsex_news_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.pulsex_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PulseX Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
