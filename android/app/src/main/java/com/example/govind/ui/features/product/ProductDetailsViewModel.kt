package com.example.govind.ui.features.product

import androidx.lifecycle.SavedStateHandle
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

data class ProductDetailsUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val error: String? = null
)

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: GovindRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle["productId"])
    
    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getProductDetails(productId).collect { product ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        product = product,
                        error = if (product == null) "Product not found" else null
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
    
    fun addToCart(quantity: Int = 1) {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            repository.addToCart(product, quantity)
        }
    }
}
