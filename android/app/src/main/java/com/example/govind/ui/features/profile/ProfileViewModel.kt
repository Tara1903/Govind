package com.example.govind.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.data.local.SessionManager
import com.example.govind.domain.repository.GovindRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "customer",
    val initial: String = "U",
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val isGuest: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: GovindRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val isLoggedIn = sessionManager.isLoggedIn
            if (isLoggedIn) {
                val result = repository.getUserProfile()
                if (result.isSuccess) {
                    val profile = result.getOrNull()
                    val name = profile?.name?.ifBlank { "Govind Customer" } ?: "Govind Customer"
                    val phone = profile?.phone ?: ""
                    val email = sessionManager.userEmail ?: ""
                    val role = profile?.role ?: sessionManager.userRole ?: "customer"
                    val initial = name.firstOrNull()?.uppercase() ?: "G"
                    _uiState.value = ProfileUiState(
                        name = name,
                        phone = phone,
                        email = email,
                        role = role,
                        initial = initial,
                        isLoading = false,
                        isGuest = false
                    )
                } else {
                    _uiState.value = ProfileUiState(
                        name = "Authenticated User",
                        phone = "",
                        role = sessionManager.userRole ?: "customer",
                        initial = "U",
                        isLoading = false,
                        isGuest = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            } else {
                _uiState.value = ProfileUiState(
                    name = "Guest User",
                    phone = "",
                    role = "customer",
                    initial = "G",
                    isLoading = false,
                    isGuest = true
                )
            }
        }
    }

    fun updatePhone(newPhone: String) {
        val trimmed = newPhone.trim()
        if (trimmed.length < 10) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid 10-digit phone number")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val res = repository.updateUserPhone(trimmed)
            if (res.isSuccess) {
                loadProfile()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = res.exceptionOrNull()?.message ?: "Failed to update phone"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = ProfileUiState(
                isLoggedOut = true,
                isGuest = true,
                name = "Guest User",
                initial = "G"
            )
        }
    }
}
