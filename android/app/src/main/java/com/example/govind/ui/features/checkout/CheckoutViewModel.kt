package com.example.govind.ui.features.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Address
import com.example.govind.data.model.Cart
import com.example.govind.data.model.Order
import com.example.govind.domain.pricing.PricingEngine
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = true,
    val cart: Cart? = null,
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null,
    val selectedPaymentMethod: String = "STARPAY_UPI",
    val itemsSubtotal: Double = 0.0,
    val totalSavings: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val grandTotal: Double = 0.0,
    val isPlacingOrder: Boolean = false,
    val orderPlaced: Boolean = false,
    val placedOrder: Order? = null,
    val error: String? = null,
    val paymentNotice: String? = null,
    val needsPhone: Boolean = false,
    val phoneSaved: Boolean = false,
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = ""
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: GovindRepository,
    private val sessionManager: com.example.govind.data.local.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadCheckoutData()
    }

    fun loadCheckoutData() {
        val loggedIn = repository.isLoggedIn()
        _uiState.value = _uiState.value.copy(
            isAuthenticated = loggedIn,
            isLoading = true,
            error = null
        )

        // Load cart and calculate pricing
        viewModelScope.launch {
            try {
                repository.getGlobalCart().collect { cart ->
                    var subtotal = 0.0
                    var savings = 0.0
                    cart?.items?.forEach { item ->
                        val result = PricingEngine.calculateProductPrice(
                            basePrice = item.product.sellingPrice,
                            quantity = item.quantity,
                            wholesalePricing = item.product.getWholesalePricing(),
                            experience = item.experienceType
                        )
                        subtotal += result.subtotal
                        val itemBasePrice = item.product.price ?: item.product.sellingPrice
                        val baseTotal = itemBasePrice * item.quantity
                        savings += (baseTotal - result.subtotal).coerceAtLeast(0.0)
                    }

                    val delivery = if (subtotal >= 500.0 || (cart?.items?.isEmpty() != false)) 0.0 else 40.0
                    val grand = subtotal + delivery

                    _uiState.value = _uiState.value.copy(
                        cart = cart,
                        itemsSubtotal = subtotal,
                        totalSavings = savings,
                        deliveryCharge = delivery,
                        grandTotal = grand,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load cart"
                )
            }
        }

        // If logged in, fetch addresses & profile
        if (loggedIn) {
            viewModelScope.launch {
                try {
                    repository.getAddresses().collect { addresses ->
                        val currentSelected = _uiState.value.selectedAddressId
                        val validSelected = if (currentSelected != null && addresses.any { it.id == currentSelected }) {
                            currentSelected
                        } else {
                            addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id
                        }
                        _uiState.value = _uiState.value.copy(
                            addresses = addresses,
                            selectedAddressId = validSelected
                        )
                    }
                } catch (e: Exception) {
                    // Ignore address fetch error in background
                }
            }

            viewModelScope.launch {
                try {
                    val profileResult = repository.getUserProfile()
                    if (profileResult.isSuccess) {
                        val profile = profileResult.getOrNull()
                        _uiState.value = _uiState.value.copy(
                            needsPhone = profile?.phone.isNullOrBlank(),
                            customerName = profile?.fullName ?: profile?.name ?: "",
                            customerEmail = sessionManager.userEmail ?: "",
                            customerPhone = profile?.phone ?: ""
                        )
                    }
                } catch (e: Exception) {
                    // Ignore profile fetch failure
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectAddress(addressId: String) {
        _uiState.value = _uiState.value.copy(selectedAddressId = addressId)
    }

    fun selectPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(
            selectedPaymentMethod = method,
            paymentNotice = null
        )
    }

    fun dismissPaymentNotice() {
        _uiState.value = _uiState.value.copy(paymentNotice = null)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun savePhoneNumber(phone: String) {
        if (phone.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateUserPhone(phone.trim())
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    needsPhone = false,
                    isLoading = false,
                    phoneSaved = true,
                    customerPhone = phone.trim()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to save phone number"
                )
            }
        }
    }

    fun placeOrder(
        onNavigateToStarPay: (amount: Double, orderId: String, orderRef: String, description: String, customerName: String, customerEmail: String, customerPhone: String) -> Unit = { _, _, _, _, _, _, _ -> }
    ) {
        val state = _uiState.value
        if (!state.isAuthenticated) {
            _uiState.value = state.copy(error = "Please sign in to place your order.")
            return
        }

        val addressId = state.selectedAddressId
        if (addressId.isNullOrBlank()) {
            _uiState.value = state.copy(error = "Please select or add a delivery address.")
            return
        }

        if (state.cart?.items.isNullOrEmpty()) {
            _uiState.value = state.copy(error = "Your basket is empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isPlacingOrder = true, error = null)
            val result = repository.placeGlobalOrder(addressId, state.selectedPaymentMethod)
            if (result.isSuccess) {
                val order = result.getOrNull()
                if (state.selectedPaymentMethod == "STARPAY_UPI" && order != null) {
                    _uiState.value = state.copy(
                        isPlacingOrder = false,
                        error = null
                    )
                    val orderRef = "GOV-${order.id.take(8).uppercase()}"
                    val selectedAddr = state.addresses.firstOrNull { it.id == addressId }
                    val phoneToUse = state.customerPhone.ifBlank { selectedAddr?.phone ?: "" }
                    val nameToUse = state.customerName.ifBlank { selectedAddr?.name ?: "Govind Customer" }
                    onNavigateToStarPay(
                        state.grandTotal,
                        order.id,
                        orderRef,
                        "GOVIND Order #$orderRef",
                        nameToUse,
                        state.customerEmail,
                        phoneToUse
                    )
                } else {
                    _uiState.value = state.copy(
                        isPlacingOrder = false,
                        orderPlaced = true,
                        placedOrder = order,
                        error = null
                    )
                }
            } else {
                _uiState.value = state.copy(
                    isPlacingOrder = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to place order. Please try again."
                )
            }
        }
    }
}
