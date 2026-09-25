package com.example.govind.data.repository

import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import com.example.govind.data.model.Cart
import com.example.govind.data.model.CartItem
import com.example.govind.domain.repository.GovindRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockGovindRepositoryImpl @Inject constructor() : GovindRepository {
    
    private val categories = listOf(
        Category(id = "1", name = "Vegetables", slug = "vegetables"),
        Category(id = "2", name = "Fruits", slug = "fruits"),
        Category(id = "3", name = "Dairy", slug = "dairy"),
    )
    
    private val products = listOf(
        Product(id = "101", name = "Tomato", slug = "tomato", price = 40.0, sellingPrice = 35.0, unit = "1kg", categoryId = "1", featured = true),
        Product(id = "102", name = "Potato", slug = "potato", price = 30.0, sellingPrice = 25.0, unit = "1kg", categoryId = "1"),
        Product(id = "201", name = "Apple", slug = "apple", price = 150.0, sellingPrice = 140.0, unit = "1kg", categoryId = "2", featured = true),
        Product(id = "301", name = "Milk", slug = "milk", price = 60.0, sellingPrice = 58.0, unit = "1L", categoryId = "3")
    )
    
    private val cartState = MutableStateFlow(Cart(id = UUID.randomUUID().toString(), profileId = "mock-user-123"))

    override fun getCategories(): Flow<List<Category>> = flowOf(categories)

    override fun getFeaturedProducts(): Flow<List<Product>> = flowOf(products.filter { it.featured })

    override fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flowOf(products.filter { it.categoryId == categoryId })

    override fun getProductDetails(productId: String): Flow<Product?> = flowOf(products.find { it.id == productId })
    
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

    private val addresses = listOf(
        com.example.govind.data.model.Address(
            id = "addr_1",
            profileId = "mock-user-123",
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

    override fun getAddresses(): Flow<List<com.example.govind.data.model.Address>> = flowOf(addresses)

    private val ordersState = MutableStateFlow<List<com.example.govind.data.model.Order>>(emptyList())

    override fun getOrders(): Flow<List<com.example.govind.data.model.Order>> = ordersState.asStateFlow()

    override suspend fun placeOrder(addressId: String, paymentMethod: String): Result<com.example.govind.data.model.Order> {
        val currentCart = cartState.value
        if (currentCart.items.isEmpty()) {
            return Result.failure(Exception("Cart is empty"))
        }

        val total = currentCart.items.sumOf { it.product.sellingPrice * it.quantity }
        val order = com.example.govind.data.model.Order(
            id = UUID.randomUUID().toString(),
            customerId = "mock-user-123",
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
