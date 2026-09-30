package com.example.govind.data.local

import androidx.room.Entity
import com.example.govind.data.model.CartItem
import com.example.govind.data.model.Product

@Entity(
    tableName = "cart_items",
    primaryKeys = ["productId", "experienceType"]
)
data class CartEntity(
    val id: String,
    val productId: String,
    val name: String,
    val slug: String,
    val price: Double,
    val sellingPrice: Double,
    val description: String?,
    val imageUrl: String?,
    val categoryId: String?,
    val unit: String,
    val quantity: Int,
    val experienceType: String = "FRESH",
    val bundleItemsJson: String? = null,
    val productType: String = "SINGLE"
) {
    fun toCartItem(): CartItem {
        val bundleObj = bundleItemsJson?.let {
            try {
                kotlinx.serialization.json.Json.parseToJsonElement(it) as? kotlinx.serialization.json.JsonObject
            } catch (e: Exception) {
                null
            }
        }
        return CartItem(
            id = productId,
            cartId = "local_cart",
            product = Product(
                id = productId,
                name = name,
                slug = slug,
                price = price,
                sellingPrice = sellingPrice,
                description = description,
                productImages = imageUrl?.let { listOf(com.example.govind.data.model.ProductImage(it)) },
                directImageUrl = imageUrl,
                categoryId = categoryId,
                unit = unit,
                experienceType = experienceType,
                productType = productType,
                bundleItems = bundleObj
            ),
            quantity = quantity,
            experienceType = experienceType
        )
    }

    companion object {
        fun fromCartItem(item: CartItem): CartEntity {
            return CartEntity(
                id = item.id,
                productId = item.product.id,
                name = item.product.name,
                slug = item.product.slug,
                price = item.product.price,
                sellingPrice = item.product.sellingPrice,
                description = item.product.description,
                imageUrl = item.product.imageUrl,
                categoryId = item.product.categoryId,
                unit = item.product.unit,
                quantity = item.quantity,
                experienceType = item.experienceType,
                bundleItemsJson = item.product.bundleItems?.toString(),
                productType = item.product.productType
            )
        }
    }
}
