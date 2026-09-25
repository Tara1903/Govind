package com.example.govind.ui.features.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Address
import com.example.govind.data.model.Cart
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val cart: Cart? = null,
    val totalAmount: Double = 0.0,
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null,
    val selectedPaymentMethod: String = "COD",
    val orderPlaced: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadCheckoutData()
    }

    private fun loadCheckoutData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getCart().collect { cart ->
                    val total = cart?.items?.sumOf { it.product.sellingPrice * it.quantity } ?: 0.0
                    _uiState.value = _uiState.value.copy(
                        cart = cart,
                        totalAmount = total
                    )
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getAddresses().collect { addresses ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        addresses = addresses,
                        selectedAddressId = addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun selectAddress(addressId: String) {
        _uiState.value = _uiState.value.copy(selectedAddressId = addressId)
    }
    
    fun selectPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }
    
    fun placeOrder() {
        val state = _uiState.value
        val addressId = state.selectedAddressId ?: return
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val result = repository.placeOrder(addressId, state.selectedPaymentMethod)
            if (result.isSuccess) {
                _uiState.value = state.copy(isLoading = false, orderPlaced = true)
            } else {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to place order"
                )
            }
        }
    }
}
