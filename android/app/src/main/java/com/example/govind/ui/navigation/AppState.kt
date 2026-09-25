package com.example.govind.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GovindExperience {
    FRESH,
    KITCHEN,
    WHOLESALE
}

object AppState {
    private val _currentExperience = MutableStateFlow(GovindExperience.FRESH)
    val currentExperience: StateFlow<GovindExperience> = _currentExperience.asStateFlow()

    fun switchExperience(experience: GovindExperience) {
        _currentExperience.value = experience
    }
}
