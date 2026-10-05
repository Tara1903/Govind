package com.example.govind

import android.util.Log
import com.example.govind.data.notification.GovindNotificationManager
import com.example.govind.data.repository.DeviceTokenRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GovindFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: GovindNotificationManager

    @Inject
    lateinit var deviceTokenRepository: DeviceTokenRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token received")
        // Register token with backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                deviceTokenRepository.registerToken(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to register token", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received from: ${message.from}")

        val data = message.data
        val notifPayload = message.notification

        // Extract fields from data payload
        val notificationId = data["notification_id"] ?: ""
        val notificationType = data["notification_type"] ?: "SYSTEM"
        val deepLink = data["deep_link"]
        val orderId = data["order_id"]
        val channel = data["channel"] ?: GovindNotificationManager.CHANNEL_ORDERS

        // Get title/body from notification payload or data
        val title = notifPayload?.title ?: data["title"] ?: "GOVIND"
        val body = notifPayload?.body ?: data["body"] ?: ""

        if (body.isNotBlank()) {
            notificationManager.showFcmNotification(
                title = title,
                body = body,
                channel = channel,
                deepLink = deepLink,
                notificationId = notificationId.ifBlank { orderId ?: System.currentTimeMillis().toString() },
                orderId = orderId
            )
        }
    }
}
