package com.example.govind.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_items WHERE experienceType = :experienceType")
    fun getCartItemsByExperience(experienceType: String): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CartEntity)

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItem(productId: String): CartEntity?

    @Query("SELECT * FROM cart_items WHERE productId = :productId AND experienceType = :experienceType LIMIT 1")
    suspend fun getCartItemForExperience(productId: String, experienceType: String): CartEntity?

    @Query("DELETE FROM cart_items WHERE productId = :productId AND experienceType = :experienceType")
    suspend fun deleteItem(productId: String, experienceType: String)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId AND experienceType = :experienceType")
    suspend fun updateQuantity(productId: String, experienceType: String, quantity: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("DELETE FROM cart_items WHERE experienceType = :experienceType")
    suspend fun clearCartForExperience(experienceType: String)
}
