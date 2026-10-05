package com.example.govind.data.starpay

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarPayApiClient @Inject constructor() {

    companion object {
        private const val TAG = "StarPayApiClient"
        const val BASE_URL = "https://payment-gateway-web-kappa.vercel.app"
        const val INTERNAL_API_KEY = "508d0154d38e49c5a4a7e489310218e77e1be6468eac4123a532ba9e0cfac26f"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun createOrder(request: StarPayCreateOrderRequest): Result<StarPayOrderData> = withContext(Dispatchers.IO) {
        try {
            val bodyString = json.encodeToString(request)
            val httpRequest = Request.Builder()
                .url("$BASE_URL/api/orders")
                .header("Content-Type", "application/json")
                .header("X-API-Key", INTERNAL_API_KEY)
                .post(bodyString.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(httpRequest).execute()
            val rawBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "createOrder failed: code=${response.code}, body=$rawBody")
                return@withContext Result.failure(Exception("StarPay createOrder failed: ${response.code} $rawBody"))
            }

            val apiResponse = json.decodeFromString<StarPayApiResponse<StarPayOrderData>>(rawBody)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.error ?: apiResponse.message ?: "Failed to create StarPay order"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "createOrder exception", e)
            Result.failure(e)
        }
    }

    suspend fun getQr(orderId: String, paymentToken: String): Result<StarPayQrData> = withContext(Dispatchers.IO) {
        try {
            val httpRequest = Request.Builder()
                .url("$BASE_URL/api/orders/$orderId/qr?token=$paymentToken")
                .header("X-Payment-Token", paymentToken)
                .get()
                .build()

            val response = client.newCall(httpRequest).execute()
            val rawBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "getQr failed: code=${response.code}, body=$rawBody")
                return@withContext Result.failure(Exception("StarPay getQr failed: ${response.code} $rawBody"))
            }

            val apiResponse = json.decodeFromString<StarPayApiResponse<StarPayQrData>>(rawBody)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.error ?: "Failed to fetch StarPay QR code"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getQr exception", e)
            Result.failure(e)
        }
    }

    suspend fun getOrderStatus(orderId: String, paymentToken: String): Result<StarPayOrderStatusData> = withContext(Dispatchers.IO) {
        try {
            val httpRequest = Request.Builder()
                .url("$BASE_URL/api/orders/$orderId?token=$paymentToken")
                .header("X-Payment-Token", paymentToken)
                .get()
                .build()

            val response = client.newCall(httpRequest).execute()
            val rawBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "getOrderStatus failed: code=${response.code}, body=$rawBody")
                return@withContext Result.failure(Exception("StarPay getOrderStatus failed: ${response.code} $rawBody"))
            }

            val apiResponse = json.decodeFromString<StarPayApiResponse<StarPayOrderStatusData>>(rawBody)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.error ?: "Failed to poll StarPay status"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getOrderStatus exception", e)
            Result.failure(e)
        }
    }

    suspend fun submitManualVerification(
        orderId: String,
        paymentToken: String,
        utrEntered: String,
        notes: String?
    ): Result<StarPayManualVerificationData> = withContext(Dispatchers.IO) {
        try {
            val request = StarPayManualVerificationRequest(
                orderId = orderId,
                utrEntered = utrEntered.trim(),
                notes = notes?.trim()
            )
            val bodyString = json.encodeToString(request)
            val httpRequest = Request.Builder()
                .url("$BASE_URL/api/manual-verifications")
                .header("Content-Type", "application/json")
                .header("X-Payment-Token", paymentToken)
                .post(bodyString.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(httpRequest).execute()
            val rawBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "submitManualVerification failed: code=${response.code}, body=$rawBody")
                return@withContext Result.failure(Exception("StarPay UTR verification failed: ${response.code} $rawBody"))
            }

            val apiResponse = json.decodeFromString<StarPayApiResponse<StarPayManualVerificationData>>(rawBody)
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.error ?: "Failed to submit manual UTR"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitManualVerification exception", e)
            Result.failure(e)
        }
    }
}
