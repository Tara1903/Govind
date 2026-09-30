package com.example.govind.ui.features.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Order
import com.example.govind.data.model.Profile
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryPartnerViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        fetchProfile()
        fetchOrders()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            repository.getUserProfile().onSuccess {
                _profile.value = it
            }
        }
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getDeliveryPartnerOrders()
                .onSuccess { 
                    _orders.value = it 
                }
                .onFailure {
                    _orders.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    fun startDelivery(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateOrderStatus(orderId, "OUT_FOR_DELIVERY")
                .onSuccess {
                    fetchOrders()
                    onSuccess()
                }
                .onFailure {
                    fetchOrders()
                }
            _isLoading.value = false
        }
    }

    fun markDelivered(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateOrderStatus(orderId, "DELIVERED")
                .onSuccess {
                    fetchOrders()
                    onSuccess()
                }
            _isLoading.value = false
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }
}
