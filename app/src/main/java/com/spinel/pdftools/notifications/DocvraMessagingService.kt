package com.spinel.pdftools.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.spinel.pdftools.MainActivity
import com.spinel.pdftools.R
import java.util.UUID

class DocvraMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received")
        // Kept ready for future backend/subscription milestone
        // Do NOT log the full token in production logs
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            val title = notification.title ?: getString(R.string.app_name)
            val body = notification.body ?: ""
            
            // Extract optional link from data payload
            val link = remoteMessage.data["play_url"] ?: remoteMessage.data["link"] ?: remoteMessage.data["url"]
            sendNotification(title, body, link)
        }

        // Also handle data-only messages if they contain title/body
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: getString(R.string.app_name)
            val body = remoteMessage.data["body"]
            if (body != null) {
                val link = remoteMessage.data["play_url"] ?: remoteMessage.data["link"] ?: remoteMessage.data["url"]
                sendNotification(title, body, link)
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String, linkUrl: String?) {
        var intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        if (!linkUrl.isNullOrEmpty() && (linkUrl.startsWith("https://play.google.com/") || linkUrl.startsWith("market://details?id="))) {
            try {
                val uri = Uri.parse(linkUrl)
                intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Invalid URL in notification payload: $linkUrl")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 
            0 /* Request code */, 
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // use a hardcoded channel ID string to avoid modifying strings.xml again for non-localized IDs
        val channelId = "general_notifications"
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Using existing app icon as requested
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a unique notification ID to show multiple notifications
        val notificationId = UUID.randomUUID().hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "DocvraMsgService"
    }
}
