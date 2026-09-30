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
    val product: Product,
    val quantity: Int,
    val experienceType: String = "FRESH"
)

@Serializable
data class CloudCartItem(
    val id: String = "",
    @SerialName("cart_id") val cartId: String = "",
    @SerialName("product_id") val productId: String = "",
    val quantity: Int = 1,
    @SerialName("experience_type") val experienceType: String = "FRESH"
)

@Serializable
data class CloudCart(
    val id: String = "",
    @SerialName("profile_id") val profileId: String = "",
    @SerialName("cart_items") val cartItems: List<CloudCartItem> = emptyList()
)

@Serializable
data class FavoriteResponse(
    val id: String = "",
    @SerialName("profile_id") val profileId: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    val products: Product? = null
)
