package com.example.govind.ui.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Order
import com.example.govind.data.model.Profile
import com.example.govind.domain.repository.GovindRepository
import com.example.govind.domain.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = true,
    val orders: List<Order> = emptyList(),
    val error: String? = null,
    val selectedOrder: Order? = null,
    val isSelectedOrderLoading: Boolean = false,
    val selectedOrderError: String? = null,
    val deliveryLocations: Map<String, com.example.govind.data.model.DeliveryLocation> = emptyMap(),
    val deliveryPartners: Map<String, Profile> = emptyMap()
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: GovindRepository,
    private val trackingRepository: TrackingRepository,
    private val notificationManager: com.example.govind.data.notification.GovindNotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val trackedOrderIds = mutableSetOf<String>()
    private val lastStatusMap = mutableMapOf<String, String>()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            if (!repository.isLoggedIn()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false,
                    orders = emptyList(),
                    error = null
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = _uiState.value.orders.isEmpty(),
                isLoggedIn = true,
                error = null
            )

            try {
                repository.getOrders().collect { orders ->
                    // Check for status changes to trigger local notifications
                    orders.forEach { order ->
                        val prev = lastStatusMap[order.id]
                        if (prev != null && !prev.equals(order.orderStatus, ignoreCase = true)) {
                            notificationManager.showOrderStatusNotification(order.id, order.orderStatus)
                        }
                        lastStatusMap[order.id] = order.orderStatus
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        orders = orders.sortedByDescending { it.createdAt }
                    )
                    
                    orders.filter { it.orderStatus == "OUT_FOR_DELIVERY" }.forEach { order ->
                        startTrackingOrder(order.id)
                        fetchDriverProfile(order.deliveryPartnerId)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load orders"
                )
            }
        }
    }

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            // Check memory cache first for instant display
            val cached = _uiState.value.orders.find { it.id == orderId }
            if (cached != null) {
                _uiState.value = _uiState.value.copy(
                    selectedOrder = cached,
                    isSelectedOrderLoading = false,
                    selectedOrderError = null
                )
                if (cached.orderStatus == "OUT_FOR_DELIVERY") {
                    startTrackingOrder(cached.id)
                }
                fetchDriverProfile(cached.deliveryPartnerId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isSelectedOrderLoading = true,
                    selectedOrderError = null
                )
            }

            // Fetch authoritative order from Supabase
            try {
                repository.getOrderById(orderId).collect { result ->
                    result.fold(
                        onSuccess = { order ->
                            _uiState.value = _uiState.value.copy(
                                selectedOrder = order,
                                isSelectedOrderLoading = false,
                                selectedOrderError = null
                            )
                            if (order.orderStatus == "OUT_FOR_DELIVERY") {
                                startTrackingOrder(order.id)
                            }
                            fetchDriverProfile(order.deliveryPartnerId)
                        },
                        onFailure = { err ->
                            if (_uiState.value.selectedOrder == null) {
                                _uiState.value = _uiState.value.copy(
                                    isSelectedOrderLoading = false,
                                    selectedOrderError = err.message ?: "Failed to load order details"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                if (_uiState.value.selectedOrder == null) {
                    _uiState.value = _uiState.value.copy(
                        isSelectedOrderLoading = false,
                        selectedOrderError = e.message ?: "Failed to load order details"
                    )
                }
            }
        }
    }

    private fun fetchDriverProfile(partnerId: String?) {
        if (partnerId.isNullOrBlank() || _uiState.value.deliveryPartners.containsKey(partnerId)) return
        viewModelScope.launch {
            repository.getProfileById(partnerId).onSuccess { profile ->
                if (profile != null) {
                    val map = _uiState.value.deliveryPartners.toMutableMap()
                    map[partnerId] = profile
                    _uiState.value = _uiState.value.copy(deliveryPartners = map)
                }
            }
        }
    }

    private fun startTrackingOrder(orderId: String) {
        if (!trackedOrderIds.contains(orderId)) {
            trackedOrderIds.add(orderId)
            viewModelScope.launch {
                trackingRepository.listenToOrderTracking(orderId).collect { loc ->
                    if (loc != null) {
                        val map = _uiState.value.deliveryLocations.toMutableMap()
                        map[orderId] = loc
                        _uiState.value = _uiState.value.copy(deliveryLocations = map)
                    }
                }
            }
        }
    }
}
