package com.example.govind.ui.features.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddAddressUiState(
    val name: String = "",
    val phone: String = "",
    val house: String = "",
    val street: String = "",
    val area: String = "",
    val city: String = "New Delhi", // default
    val pincode: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddAddressViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAddressUiState())
    val uiState = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        val current = _uiState.value
        _uiState.value = when (field) {
            "name" -> current.copy(name = value)
            "phone" -> current.copy(phone = value)
            "house" -> current.copy(house = value)
            "street" -> current.copy(street = value)
            "area" -> current.copy(area = value)
            "city" -> current.copy(city = value)
            "pincode" -> current.copy(pincode = value)
            else -> current
        }
    }

    fun saveAddress() {
        val state = uiState.value
        if (state.name.isBlank() || state.phone.isBlank() || state.house.isBlank() || state.area.isBlank() || state.pincode.isBlank()) {
            _uiState.value = state.copy(error = "Please fill all required fields")
            return
        }
        
        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = repository.addAddress(
                name = state.name,
                phone = state.phone,
                house = state.house,
                street = state.street,
                area = state.area,
                city = state.city,
                pincode = state.pincode
            )
            if (result.isSuccess) {
                _uiState.value = state.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = state.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to save address")
            }
        }
    }
}
