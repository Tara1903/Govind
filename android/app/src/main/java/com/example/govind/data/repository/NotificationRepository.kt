package com.example.govind.data.repository

import android.util.Log
import com.example.govind.data.remote.SupabaseApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class NotificationItem(
    val id: String,
    @SerialName("user_id") val userId: String = "",
    val type: String = "SYSTEM",
    val title: String,
    val body: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("deep_link") val deepLink: String? = null,
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("expires_at") val expiresAt: String? = null
) {
    val isRead: Boolean get() = readAt != null
}

@Singleton
class NotificationRepository @Inject constructor(
    private val supabaseApi: SupabaseApi
) {
    suspend fun getNotifications(limit: Int = 50): List<NotificationItem> {
        return try {
            supabaseApi.getNotifications(limit = limit)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to fetch notifications", e)
            emptyList()
        }
    }

    suspend fun getUnreadCount(): Int {
        return try {
            val result = supabaseApi.getUnreadNotifications()
            result.size
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to get unread count", e)
            0
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            val request = buildJsonObject {
                put("p_notification_id", notificationId)
            }
            supabaseApi.markNotificationRead(request)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to mark read", e)
        }
    }

    suspend fun markAllAsRead() {
        try {
            supabaseApi.markAllNotificationsRead()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Failed to mark all read", e)
        }
    }
}
