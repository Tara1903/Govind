const fs = require('fs');
const path = require('path');

console.log('Starting Phase 3 patch...');

// 1. Fix ProfileViewModel.kt
const profileVmPath = path.resolve('android/app/src/main/java/com/example/govind/ui/features/profile/ProfileViewModel.kt');
let profileVm = fs.readFileSync(profileVmPath, 'utf8');
profileVm = profileVm.replace(
    'val email = profile?.email ?: ""',
    'val email = sessionManager.userEmail ?: ""'
);
fs.writeFileSync(profileVmPath, profileVm, 'utf8');
console.log('1. ProfileViewModel patched.');

// 2. Fix HomeViewModel.kt
const homeVmPath = path.resolve('android/app/src/main/java/com/example/govind/ui/features/home/HomeViewModel.kt');
const homeVmContent = `package com.example.govind.ui.features.home

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
}
`;
fs.writeFileSync(homeVmPath, homeVmContent, 'utf8');
console.log('2. HomeViewModel updated.');

// 3. Fix KitchenViewModel.kt
const kitchenVmPath = path.resolve('android/app/src/main/java/com/example/govind/ui/features/kitchen/KitchenViewModel.kt');
let kitchenVm = fs.readFileSync(kitchenVmPath, 'utf8');
const oldPriceMatchRegex = /val priceMatch = when \(activePriceFilter\) \{[\s\S]*?else -> true\s*\}/;
const newPriceMatch = `val priceMatch = when {
                    (activePriceFilter.contains("100") && (activePriceFilter.contains("Under", ignoreCase = true) || activePriceFilter.contains("<"))) -> product.sellingPrice < 100
                    (activePriceFilter.contains("100") && activePriceFilter.contains("300")) -> product.sellingPrice in 100.0..300.0
                    (activePriceFilter.contains("300") && (activePriceFilter.contains("Above", ignoreCase = true) || activePriceFilter.contains(">"))) -> product.sellingPrice > 300
                    else -> true
                }`;
kitchenVm = kitchenVm.replace(oldPriceMatchRegex, newPriceMatch);
fs.writeFileSync(kitchenVmPath, kitchenVm, 'utf8');
console.log('3. KitchenViewModel updated.');

// 4. Fix WholesaleViewModel.kt
const wholesaleVmPath = path.resolve('android/app/src/main/java/com/example/govind/ui/features/wholesale/WholesaleViewModel.kt');
let wholesaleVm = fs.readFileSync(wholesaleVmPath, 'utf8');
const oldFallbackRegex = /if \(products\.isEmpty\(\)\) \{[\s\S]*?fallbackItems[\s\S]*?\} else \{[\s\S]*?\}/;
const newWholesaleHandling = `_uiState.value = _uiState.value.copy(items = products, isLoading = false)`;
wholesaleVm = wholesaleVm.replace(oldFallbackRegex, newWholesaleHandling);
fs.writeFileSync(wholesaleVmPath, wholesaleVm, 'utf8');
console.log('4. WholesaleViewModel updated.');

console.log('Phase 3 preliminary patch completed successfully.');
