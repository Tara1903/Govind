package com.example.govind.ui.features.kitchen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun KitchenHomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Govind Kitchen Home (Phase 3)", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun KitchenMenuScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Govind Kitchen Menu", style = MaterialTheme.typography.headlineMedium)
    }
}
