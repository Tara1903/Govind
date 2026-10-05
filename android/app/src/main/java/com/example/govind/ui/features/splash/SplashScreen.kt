package com.example.govind.ui.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.ui.layout.ContentScale
import com.example.govind.theme.GovindTheme

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToDeliveryPartner: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(1000L) // Simulate some loading time
        if (!viewModel.hasCompletedOnboarding()) {
            onNavigateToOnboarding()
        } else if (viewModel.isDeliveryPartner()) {
            onNavigateToDeliveryPartner()
        } else if (viewModel.isLoggedIn() || viewModel.isGuest()) {
            onNavigateToHome()
        } else {
            onNavigateToAuth()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GovindTheme.colors.warmWhite),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = com.example.govind.R.drawable.govind_logo_squircle),
            contentDescription = "Govind Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(180.dp)
        )
    }
}

