package com.example.govind.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.govind.domain.repository.GovindRepository
import com.example.govind.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val otpSent: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val userRole: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: GovindRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun sendOtp() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val res = repository.sendOtp(email)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, otpSent = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = res.exceptionOrNull()?.message ?: "Failed to send OTP"
                )
            }
        }
    }

    fun verifyOtp(otp: String) {
        val email = _uiState.value.email.trim()
        if (otp.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid 6-digit OTP")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val res = repository.verifyOtp(email, otp)
            if (res.isSuccess) {
                sessionManager.isGuest = false
                val role = sessionManager.userRole
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, userRole = role)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = res.exceptionOrNull()?.message ?: "Invalid OTP"
                )
            }
        }
    }

    fun changeEmail() {
        _uiState.value = _uiState.value.copy(otpSent = false, error = null)
    }

    fun continueAsGuest() {
        sessionManager.isGuest = true
    }
}
