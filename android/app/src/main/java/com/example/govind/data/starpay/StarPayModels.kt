package com.example.govind.data.starpay

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StarPayOrderStatus {
    @SerialName("CREATED")
    CREATED,
    @SerialName("AWAITING_PAYMENT")
    AWAITING_PAYMENT,
    @SerialName("VERIFYING")
    VERIFYING,
    @SerialName("PAID")
    PAID,
    @SerialName("PENDING_VERIFICATION")
    PENDING_VERIFICATION,
    @SerialName("FAILED")
    FAILED,
    @SerialName("REFUNDED")
    REFUNDED,
    UNKNOWN;

    companion object {
        fun fromString(status: String?): StarPayOrderStatus {
            return when (status?.trim()?.uppercase()) {
                "CREATED" -> CREATED
                "AWAITING_PAYMENT" -> AWAITING_PAYMENT
                "VERIFYING" -> VERIFYING
                "PAID" -> PAID
                "PENDING_VERIFICATION" -> PENDING_VERIFICATION
                "FAILED" -> FAILED
                "REFUNDED" -> REFUNDED
                else -> UNKNOWN
            }
        }
    }
}

@Serializable
data class StarPayCreateOrderRequest(
    val amount: Double,
    val currency: String = "INR",
    val description: String? = null,
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val metadata: Map<String, String>? = null,
    val webhookUrl: String? = null
)

@Serializable
data class StarPayApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

@Serializable
data class StarPayOrderData(
    val orderId: String,
    val orderRef: String,
    val amount: Double,
    val reservedAmount: Double,
    val currency: String = "INR",
    val paymentToken: String,
    val upiTxnRef: String? = null,
    val expiresAt: String,
    val checkoutUrl: String? = null
)

@Serializable
data class StarPayQrData(
    val qrDataUrl: String,
    val upiUrl: String,
    val upiId: String,
    val amount: Double,
    val expiresAt: String
)

@Serializable
data class StarPayOrderStatusData(
    val orderId: String,
    val orderRef: String,
    val amount: Double,
    val reservedAmount: Double,
    val currency: String = "INR",
    val description: String? = null,
    val status: String,
    val upiTxnRef: String? = null,
    val expiresAt: String,
    val paidAt: String? = null,
    val createdAt: String? = null
)

@Serializable
data class StarPayManualVerificationRequest(
    val orderId: String,
    val utrEntered: String,
    val notes: String? = null
)

@Serializable
data class StarPayManualVerificationData(
    val manualVerificationId: String? = null,
    val status: String? = null
)
