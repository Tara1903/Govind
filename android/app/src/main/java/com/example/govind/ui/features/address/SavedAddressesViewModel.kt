package com.example.govind.ui.features.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.model.Address
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedAddressesUiState(
    val isLoading: Boolean = true,
    val addresses: List<Address> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SavedAddressesViewModel @Inject constructor(
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedAddressesUiState())
    val uiState: StateFlow<SavedAddressesUiState> = _uiState.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getAddresses().collect { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        addresses = list,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load addresses"
                )
            }
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            repository.deleteAddress(id)
        }
    }

    fun setDefault(id: String) {
        viewModelScope.launch {
            repository.setDefaultAddress(id)
        }
    }

    fun updateAddress(
        id: String,
        name: String,
        phone: String,
        house: String,
        street: String,
        area: String,
        landmark: String?,
        city: String,
        pincode: String,
        isDefault: Boolean
    ) {
        viewModelScope.launch {
            repository.updateAddress(id, name, phone, house, street, area, landmark, city, pincode, isDefault)
        }
    }

    fun isUserLoggedIn(): Boolean = repository.isUserLoggedIn()
}
