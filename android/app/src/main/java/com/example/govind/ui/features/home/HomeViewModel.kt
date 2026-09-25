package com.example.govind.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                repository.getCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                }
                
                repository.getFeaturedProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        featuredProducts = products,
                        isLoading = false
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

    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            repository.addToCart(product, quantity)
        }
    }
}
