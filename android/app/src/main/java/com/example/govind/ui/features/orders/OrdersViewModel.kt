package com.example.govind.ui.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Order
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getOrders().collect { orders ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        orders = orders
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load orders"
                )
            }
        }
    }
}
