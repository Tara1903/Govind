package com.example.govind.ui.features.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Cart
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = true,
    val cart: Cart? = null,
    val totalAmount: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        loadCart()
    }

    private fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getCart().collect { cart ->
                    val total = cart?.items?.sumOf { it.product.sellingPrice * it.quantity } ?: 0.0
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cart = cart,
                        totalAmount = total
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unknown error occurred"
                )
            }
        }
    }
    
    fun increaseQuantity(cartItemId: String, currentQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(cartItemId, currentQuantity + 1)
        }
    }
    
    fun decreaseQuantity(cartItemId: String, currentQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartItemQuantity(cartItemId, currentQuantity - 1)
        }
    }
}
