package com.example.govind.ui.features.wholesale

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun WholesaleHomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Govind Wholesale Home (Phase 6)", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun WholesaleCatalogScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Govind Wholesale Catalog", style = MaterialTheme.typography.headlineMedium)
    }
}
