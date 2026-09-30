package com.example.govind.ui.features.wholesale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Product
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WholesaleUiState(
    val isLoading: Boolean = true,
    val items: List<Product> = emptyList(),
    val cartQuantities: Map<String, Int> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class WholesaleViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WholesaleUiState())
    val uiState: StateFlow<WholesaleUiState> = _uiState.asStateFlow()

    init {
        loadWholesaleItems()
        observeCart()
    }

    private fun loadWholesaleItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getWholesaleItems().collect { products ->
                    _uiState.value = _uiState.value.copy(items = products, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load wholesale items"
                )
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            try {
                repository.getGlobalCart().collect { cart ->
                    val quantities = cart?.items
                        ?.filter { it.experienceType == "WHOLESALE" }
                        ?.associate { it.product.id to it.quantity } ?: emptyMap()
                    _uiState.value = _uiState.value.copy(cartQuantities = quantities)
                }
            } catch (e: Exception) { /* log */ }
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product, quantity, "WHOLESALE")
        }
    }

    fun updateQuantity(product: Product, newQuantity: Int) {
        viewModelScope.launch {
            if (newQuantity <= 0) {
                repository.removeFromCart(product.id, "WHOLESALE")
            } else {
                repository.updateCartItemQuantity(product.id, "WHOLESALE", newQuantity)
            }
        }
    }

    fun retry() {
        loadWholesaleItems()
    }
}
