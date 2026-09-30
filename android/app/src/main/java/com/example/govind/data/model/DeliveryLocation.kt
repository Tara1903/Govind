package com.example.govind.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryLocation(
    val order_id: String,
    val estimated_distance_m: Int = 0,
    val estimated_time_sec: Int = 0,
    val updated_at: String = "",
    val delivery_partner_id: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Double? = null,
    val speed: Double? = null,
    val heading: Double? = null
)
