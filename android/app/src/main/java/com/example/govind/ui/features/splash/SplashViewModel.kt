package com.example.govind.ui.features.splash

import androidx.lifecycle.ViewModel
import com.example.govind.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn
}
