package com.example.govind.ui.features.onboarding

import androidx.lifecycle.ViewModel
import com.example.govind.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    fun completeOnboarding() {
        sessionManager.hasCompletedOnboarding = true
    }
}
