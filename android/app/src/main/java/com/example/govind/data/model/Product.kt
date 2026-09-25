package com.example.govind.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val price: Double,
    @SerialName("selling_price") val sellingPrice: Double,
    val unit: String,
    @SerialName("stock_quantity") val stockQuantity: Int = 0,
    @SerialName("low_stock_threshold") val lowStockThreshold: Int = 5,
    @SerialName("min_order_quantity") val minOrderQuantity: Int = 1,
    @SerialName("max_order_quantity") val maxOrderQuantity: Int = 10,
    @SerialName("discount_type") val discountType: String? = null,
    @SerialName("discount_value") val discountValue: Double? = null,
    val active: Boolean = true,
    val featured: Boolean = false,
    val bestseller: Boolean = false,
    val seasonal: Boolean = false,
    @SerialName("fresh_today") val freshToday: Boolean = false,
    val imageUrl: String? = null
)
