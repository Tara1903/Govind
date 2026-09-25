package com.example.govind.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
    @SerialName("profile_id") val profileId: String,
    val name: String,
    val phone: String,
    val house: String,
    val street: String,
    val area: String,
    val landmark: String? = null,
    val city: String,
    val pincode: String,
    @SerialName("delivery_instructions") val deliveryInstructions: String? = null,
    @SerialName("is_default") val isDefault: Boolean = false
)

@Serializable
data class Order(
    val id: String,
    @SerialName("customer_id") val customerId: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    @SerialName("delivery_charge") val deliveryCharge: Double = 0.0,
    val total: Double,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("payment_status") val paymentStatus: String,
    @SerialName("order_status") val orderStatus: String
)
