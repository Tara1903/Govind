package com.example.govind.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
data class PaymentGatewayRequest(
    val amount: Double,
    val currency: String = "INR",
    val description: String? = null,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val returnUrl: String? = null
)

@Serializable
data class PaymentGatewayResponseData(
    val checkoutUrl: String,
    val orderId: String,
    val orderRef: String
)

@Serializable
data class PaymentGatewayResponse(
    val success: Boolean,
    val data: PaymentGatewayResponseData? = null
)

interface PaymentGatewayApi {
    @POST("api/orders")
    suspend fun createOrder(
        @Header("X-API-Key") apiKey: String,
        @Body request: PaymentGatewayRequest
    ): PaymentGatewayResponse
}
