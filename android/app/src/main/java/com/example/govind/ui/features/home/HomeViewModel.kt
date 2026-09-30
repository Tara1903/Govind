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
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val freshBoardProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val punjabiMenuProducts: List<Product> = emptyList(),
    val packs: List<Product> = emptyList(),
    val combos: List<Product> = emptyList(),
    val cartQuantities: Map<String, Int> = emptyMap(),
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Launch concurrent data loaders
            launch {
                try {
                    repository.getFreshBoardProducts().collect { freshProducts ->
                        _uiState.value = _uiState.value.copy(freshBoardProducts = freshProducts)
                    }
                } catch (e: Exception) { /* log */ }
            }

            launch {
                try {
                    repository.getCategories().collect { allCategories ->
                        val freshCategories = allCategories.filter { it.experienceType == "FRESH" || it.experienceType == null }
                        _uiState.value = _uiState.value.copy(categories = freshCategories)
                    }
                } catch (e: Exception) { /* log */ }
            }

            launch {
                try {
                    repository.getKitchenMenuItems().collect { kitchenProducts ->
                        _uiState.value = _uiState.value.copy(punjabiMenuProducts = kitchenProducts)
                    }
                } catch (e: Exception) { /* log */ }
            }

            launch {
                try {
                    repository.getPacks().collect { packs ->
                        _uiState.value = _uiState.value.copy(packs = packs)
                    }
                } catch (e: Exception) { /* log */ }
            }

            launch {
                try {
                    repository.getCombos().collect { combos ->
                        _uiState.value = _uiState.value.copy(combos = combos)
                    }
                } catch (e: Exception) { /* log */ }
            }

            launch {
                try {
                    repository.getGlobalCart().collect { cart ->
                        val quantities = cart?.items?.associate { it.product.id to it.quantity } ?: emptyMap()
                        _uiState.value = _uiState.value.copy(cartQuantities = quantities)
                    }
                } catch (e: Exception) { /* log */ }
            }

            launch {
                try {
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
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val exp = if (product.experienceType != null) product.experienceType else com.example.govind.ui.navigation.AppState.currentExperience.value.name
            repository.addToCart(product, quantity, exp)
        }
    }

    fun updateQuantity(product: Product, newQuantity: Int) {
        viewModelScope.launch {
            val exp = if (product.experienceType != null) product.experienceType else com.example.govind.ui.navigation.AppState.currentExperience.value.name
            if (newQuantity <= 0) {
                repository.removeFromCart(product.id, exp)
            } else {
                repository.updateCartItemQuantity(product.id, exp, newQuantity)
            }
        }
    }
}
