package com.example.govind.ui.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Product
import com.example.govind.domain.repository.GovindRepository
import com.example.govind.ui.navigation.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val items: List<Product> = emptyList(),
    val cartQuantities: Map<String, Int> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            repository.getGlobalCart().collect { cart ->
                val quantities = cart?.items?.associate { it.product.id to it.quantity } ?: emptyMap()
                _uiState.value = _uiState.value.copy(cartQuantities = quantities)
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getFavorites().collect { favs ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = favs,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load favorites"
                )
            }
        }
    }

    fun removeFavorite(productId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val exp = product.experienceType.ifBlank { AppState.currentExperience.value.name }
            repository.addToCart(product, quantity, exp)
        }
    }

    fun updateQuantity(product: Product, quantity: Int) {
        viewModelScope.launch {
            val exp = product.experienceType.ifBlank { AppState.currentExperience.value.name }
            if (quantity <= 0) {
                repository.removeFromCart(product.id, exp)
            } else {
                repository.updateCartItemQuantity(product.id, exp, quantity)
            }
        }
    }

    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()
}
