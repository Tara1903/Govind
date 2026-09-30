package com.example.govind.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.govind.MainActivity
import com.example.govind.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GovindNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ORDER_UPDATES = "govind_order_updates"
        const val CHANNEL_NAME = "Order Updates & Tracking"
        const val CHANNEL_DESC = "Notifications for Govind order status transitions and live tracking updates."
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ORDER_UPDATES,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showOrderStatusNotification(orderId: String, status: String) {
        val (title, body) = when (status.uppercase()) {
            "CONFIRMED" -> "Order Confirmed! 🍲" to "Your Govind order #${orderId.take(8)} has been confirmed."
            "PREPARING" -> "Preparing Your Order 👨‍🍳" to "Fresh produce is being packed and kitchen dishes are freshly cooking."
            "READY_FOR_DELIVERY" -> "Order Ready for Pickup 📦" to "Your items are packaged and waiting for the delivery partner."
            "OUT_FOR_DELIVERY" -> "Out for Delivery! 🛵" to "Your delivery partner is en route! Tap to view live ETA & tracking."
            "DELIVERED" -> "Delivered! 🎉" to "Your Govind order has been delivered. Enjoy fresh and authentic flavors!"
            "CANCELLED" -> "Order Cancelled" to "Your order #${orderId.take(8)} has been cancelled."
            else -> "Order Update" to "Your order status is now $status."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "order_details/$orderId")
            putExtra("orderId", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ORDER_UPDATES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(orderId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS might not be granted by user yet
        }
    }
}
