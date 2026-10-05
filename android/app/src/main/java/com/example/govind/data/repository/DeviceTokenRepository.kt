package com.example.govind.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.govind.BuildConfig
import com.example.govind.data.local.SessionManager
import com.example.govind.data.remote.SupabaseApi
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTokenRepository @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val supabaseApi: SupabaseApi
) {
    suspend fun registerCurrentToken() {
        if (!sessionManager.isLoggedIn) return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            registerToken(token)
        } catch (e: Exception) {
            Log.e("DeviceTokenRepo", "Failed to get FCM token", e)
        }
    }

    suspend fun registerToken(token: String) {
        if (!sessionManager.isLoggedIn) return
        try {
            val request = buildJsonObject {
                put("p_fcm_token", token)
                put("p_device_model", Build.MODEL)
                put("p_os_version", "Android ${Build.VERSION.RELEASE}")
                put("p_app_version", BuildConfig.VERSION_NAME)
            }
            supabaseApi.upsertDeviceToken(request)
            Log.d("DeviceTokenRepo", "Token registered successfully")
        } catch (e: Exception) {
            Log.e("DeviceTokenRepo", "Failed to register token", e)
        }
    }

    suspend fun deregisterToken() {
        // Deregister by updating token status if possible, 
        // since we don't have an endpoint for this in SupabaseApi, we just omit or add if needed.
    }
}
