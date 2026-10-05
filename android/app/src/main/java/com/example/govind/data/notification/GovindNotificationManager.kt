package com.example.govind.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
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
        // Channels
        const val CHANNEL_ORDERS = "govind_orders"
        const val CHANNEL_DELIVERY = "govind_delivery"
        const val CHANNEL_CART = "govind_cart"
        const val CHANNEL_OFFERS = "govind_offers"
        const val CHANNEL_MARKETING = "govind_marketing"
        const val CHANNEL_ACCOUNT = "govind_account"
        const val CHANNEL_SYSTEM = "govind_system"
        // Legacy channel name kept for backward compatibility
        const val CHANNEL_ORDER_UPDATES = "govind_order_updates"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(CHANNEL_ORDERS, "Order Updates", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Order status and tracking updates"
                enableVibration(true)
            },
            NotificationChannel(CHANNEL_DELIVERY, "Delivery Tracking", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Rider location and delivery updates"
                enableVibration(true)
            },
            NotificationChannel(CHANNEL_CART, "Cart Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Reminders about items in your cart"
            },
            NotificationChannel(CHANNEL_OFFERS, "Offers & Deals", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Fresh deals and seasonal offers"
            },
            NotificationChannel(CHANNEL_MARKETING, "News & Updates", NotificationManager.IMPORTANCE_LOW).apply {
                description = "GOVIND news and announcements"
            },
            NotificationChannel(CHANNEL_ACCOUNT, "Account", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Account and security updates"
            },
            NotificationChannel(CHANNEL_SYSTEM, "System", NotificationManager.IMPORTANCE_LOW).apply {
                description = "System notifications"
            },
            // Legacy
            NotificationChannel(CHANNEL_ORDER_UPDATES, "Order Tracking", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Order and delivery updates"
            },
        )
        channels.forEach { nm.createNotificationChannel(it) }
    }

    fun showFcmNotification(
        title: String,
        body: String,
        channel: String = CHANNEL_ORDERS,
        deepLink: String? = null,
        notificationId: String = "",
        orderId: String? = null
    ) {
        val intent = createDeepLinkIntent(context, deepLink, orderId)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF38802A.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(when (channel) {
                CHANNEL_ORDERS, CHANNEL_DELIVERY -> NotificationCompat.PRIORITY_HIGH
                CHANNEL_MARKETING, CHANNEL_SYSTEM -> NotificationCompat.PRIORITY_LOW
                else -> NotificationCompat.PRIORITY_DEFAULT
            })
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId.hashCode().let { if (it == 0) 1 else it }, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    fun showOrderStatusNotification(orderId: String, status: String) {
        val title = when (status.uppercase()) {
            "PLACED"             -> "Order placed successfully"
            "CONFIRMED"          -> "Order confirmed \u2705"
            "PREPARING"          -> "Your order is being prepared"
            "READY_FOR_DELIVERY" -> "Order packed & ready \uD83D\uDCE6"
            "OUT_FOR_DELIVERY"   -> "Your order is on the way \uD83D\uDEB4"
            "DELIVERED"          -> "Order delivered \uD83C\uDF89"
            "CANCELLED"          -> "Order cancelled"
            else                 -> "Order update"
        }
        val body = when (status.uppercase()) {
            "OUT_FOR_DELIVERY" -> "Track your rider in real time."
            "DELIVERED"        -> "Your GOVIND order has arrived!"
            else               -> "Your GOVIND order #${orderId.take(8).uppercase()} has been updated."
        }
        val channel = if (status.uppercase() == "OUT_FOR_DELIVERY" || status.uppercase() == "DELIVERED")
            CHANNEL_DELIVERY else CHANNEL_ORDERS
        val deepLink = if (status.uppercase() == "OUT_FOR_DELIVERY")
            "govind://order/$orderId/tracking" else "govind://order/$orderId"

        showFcmNotification(title, body, channel, deepLink, orderId, orderId)
    }

    fun createDeepLinkIntent(context: Context, deepLink: String?, orderId: String? = null): Intent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
        }

        if (!deepLink.isNullOrBlank()) {
            intent.data = Uri.parse(deepLink)
            // Also put extras for fallback navigation
            when {
                deepLink.contains("/tracking") -> {
                    val id = deepLink.substringAfter("order/").substringBefore("/tracking")
                    intent.putExtra("route", "order_details/$id")
                    intent.putExtra("orderId", id)
                    intent.putExtra("showTracking", true)
                }
                deepLink.startsWith("govind://order/") -> {
                    val id = deepLink.substringAfter("govind://order/")
                    intent.putExtra("route", "order_details/$id")
                    intent.putExtra("orderId", id)
                }
                deepLink == "govind://cart" -> {
                    intent.putExtra("route", "cart")
                }
                deepLink == "govind://notifications" -> {
                    intent.putExtra("route", "notifications")
                }
                deepLink.startsWith("govind://product/") -> {
                    val id = deepLink.substringAfter("govind://product/")
                    intent.putExtra("route", "product/$id")
                    intent.putExtra("productId", id)
                }
            }
        } else if (orderId != null) {
            intent.putExtra("route", "order_details/$orderId")
            intent.putExtra("orderId", orderId)
        }

        return intent
    }
}
