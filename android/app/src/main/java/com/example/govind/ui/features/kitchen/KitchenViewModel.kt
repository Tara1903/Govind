package com.example.govind.ui.features.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Product
import com.example.govind.data.model.Cart
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KitchenUiState(
    val isLoading: Boolean = false,
    val menuItems: List<Product> = emptyList(),
    val categories: List<com.example.govind.data.model.Category> = emptyList(),
    val cart: Cart? = null,
    val error: String? = null,
    val activeCategory: String = "All",
    val activePriceFilter: String = "All"
) {
    val filteredItems: List<Product>
        get() {
            return menuItems.filter { product ->
                val categoryIdForActive = categories.find { it.name == activeCategory }?.id
                val categoryMatch = activeCategory == "All" || product.categoryId == categoryIdForActive
                val priceMatch = when {
                    (activePriceFilter.contains("100") && (activePriceFilter.contains("Under", ignoreCase = true) || activePriceFilter.contains("<"))) -> product.sellingPrice < 100
                    (activePriceFilter.contains("100") && activePriceFilter.contains("300")) -> product.sellingPrice in 100.0..300.0
                    (activePriceFilter.contains("300") && (activePriceFilter.contains("Above", ignoreCase = true) || activePriceFilter.contains(">"))) -> product.sellingPrice > 300
                    else -> true
                }
                categoryMatch && priceMatch
            }
        }

    val cartQuantities: Map<String, Int>
        get() = cart?.items?.associate { it.product.id to it.quantity } ?: emptyMap()
}

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitchenUiState(isLoading = true))
    val uiState: StateFlow<KitchenUiState> = _uiState.asStateFlow()

    init {
        loadMenuItems()
        observeCart()
    }

    private fun loadMenuItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            launch {
                repository.getCategories().collect { cats ->
                    _uiState.value = _uiState.value.copy(categories = cats)
                }
            }
            launch {
                repository.getKitchenMenuItems().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        menuItems = products
                    )
                }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            repository.getGlobalCart().collect { cart ->
                _uiState.value = _uiState.value.copy(cart = cart)
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            repository.addToCart(product, 1, "KITCHEN")
        }
    }

    fun updateQuantity(product: Product, newQuantity: Int) {
        viewModelScope.launch {
            if (newQuantity <= 0) {
                repository.removeFromCart(product.id, "KITCHEN")
            } else {
                repository.updateCartItemQuantity(product.id, "KITCHEN", newQuantity)
            }
        }
    }

    fun setActiveCategory(category: String) {
        _uiState.value = _uiState.value.copy(activeCategory = category)
    }

    fun setActivePriceFilter(filter: String) {
        _uiState.value = _uiState.value.copy(activePriceFilter = filter)
    }

    fun retry() {
        loadMenuItems()
    }
}


