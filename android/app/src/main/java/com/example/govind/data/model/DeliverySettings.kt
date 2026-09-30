package com.example.govind.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeliverySettings(
    val id: String,
    @SerialName("min_order_amount") val minOrderAmount: Double = 100.0,
    @SerialName("delivery_charge") val deliveryCharge: Double = 40.0,
    @SerialName("free_delivery_threshold") val freeDeliveryThreshold: Double = 500.0,
    val active: Boolean = true
)
