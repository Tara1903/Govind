package com.example.govind.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cart(
    val id: String,
    @SerialName("profile_id") val profileId: String,
    val items: List<CartItem> = emptyList()
)

@Serializable
data class CartItem(
    val id: String,
    @SerialName("cart_id") val cartId: String,
    val product: Product, // In a real app we might only store product_id locally and fetch details, but let's keep it simple here
    val quantity: Int
)
