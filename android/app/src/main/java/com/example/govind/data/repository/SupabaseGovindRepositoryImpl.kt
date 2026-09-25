package com.example.govind.data.repository

import com.example.govind.data.model.Address
import com.example.govind.data.model.Cart
import com.example.govind.data.model.CartItem
import com.example.govind.data.model.Category
import com.example.govind.data.model.Order
import com.example.govind.data.model.Product
import com.example.govind.data.remote.SupabaseApi
import com.example.govind.domain.repository.GovindRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseGovindRepositoryImpl @Inject constructor(
    private val api: SupabaseApi
) : GovindRepository {

    // Mock state for Guest Cart and Orders until Auth is implemented
    private val cartState = MutableStateFlow(Cart(id = UUID.randomUUID().toString(), profileId = "guest-123"))
    private val ordersState = MutableStateFlow<List<Order>>(emptyList())
    private val addresses = listOf(
        Address(
            id = "addr_1",
            profileId = "guest-123",
            name = "Home",
            phone = "9876543210",
            house = "Flat 101",
            street = "Main Street",
            area = "Downtown",
            city = "Cityville",
            pincode = "123456",
            isDefault = true
        )
    )

    override fun getCategories(): Flow<List<Category>> = flow {
        emit(api.getCategories())
    }

    override fun getFeaturedProducts(): Flow<List<Product>> = flow {
        emit(api.getFeaturedProducts())
    }

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow {
        emit(api.getProductsByCategory("eq.$categoryId"))
    }

    override fun getProductDetails(productId: String): Flow<Product?> = flow {
        val result = api.getProductDetails("eq.$productId")
        emit(result.firstOrNull())
    }

    override fun getCart(): Flow<Cart?> = cartState.asStateFlow()

    override suspend fun addToCart(product: Product, quantity: Int) {
        val currentCart = cartState.value
        val existingItemIndex = currentCart.items.indexOfFirst { it.product.id == product.id }

        val newItems = currentCart.items.toMutableList()
        if (existingItemIndex >= 0) {
            val existingItem = newItems[existingItemIndex]
            newItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            newItems.add(CartItem(id = UUID.randomUUID().toString(), cartId = currentCart.id, product = product, quantity = quantity))
        }

        cartState.value = currentCart.copy(items = newItems)
    }

    override suspend fun removeFromCart(cartItemId: String) {
        val currentCart = cartState.value
        cartState.value = currentCart.copy(items = currentCart.items.filter { it.id != cartItemId })
    }

    override suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int) {
        val currentCart = cartState.value
        if (quantity <= 0) {
            removeFromCart(cartItemId)
            return
        }

        cartState.value = currentCart.copy(
            items = currentCart.items.map { if (it.id == cartItemId) it.copy(quantity = quantity) else it }
        )
    }

    override fun getAddresses(): Flow<List<Address>> = flowOf(addresses)

    override fun getOrders(): Flow<List<Order>> = ordersState.asStateFlow()

    override suspend fun placeOrder(addressId: String, paymentMethod: String): Result<Order> {
        val currentCart = cartState.value
        if (currentCart.items.isEmpty()) {
            return Result.failure(Exception("Cart is empty"))
        }

        val total = currentCart.items.sumOf { it.product.sellingPrice * it.quantity }
        val order = Order(
            id = UUID.randomUUID().toString(),
            customerId = "guest-123",
            subtotal = total,
            total = total + 40.0, // Assuming 40 delivery charge
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == "COD") "PENDING" else "SUCCESS",
            orderStatus = "CONFIRMED"
        )

        // Clear cart
        cartState.value = currentCart.copy(items = emptyList())
        // Add to orders
        ordersState.value = ordersState.value + order

        return Result.success(order)
    }
}
