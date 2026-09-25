package com.example.govind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.govind.data.model.CartItem
import com.example.govind.data.model.Product

@Entity(tableName = "cart_items")
data class CartEntity(
    val id: String,
    @PrimaryKey
    val productId: String,
    val name: String,
    val slug: String,
    val price: Double,
    val sellingPrice: Double,
    val description: String?,
    val imageUrl: String?,
    val categoryId: String?,
    val unit: String,
    val quantity: Int
) {
    fun toCartItem(): CartItem {
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
                imageUrl = imageUrl,
                categoryId = categoryId,
                unit = unit
            ),
            quantity = quantity
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
                quantity = item.quantity
            )
        }
    }
}
