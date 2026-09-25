package com.example.govind.ui.features.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val product = uiState.product

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
                    ) {
                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (product != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "₹${product.sellingPrice}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (product.price != null && product.price > product.sellingPrice) {
                                Text(
                                    text = "MRP ₹${product.price}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.addToCart(1) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 24.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Add to Cart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (product != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.White)
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(32.dp)
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Product Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description ?: "Fresh and high quality product sourced directly from farms to your doorstep.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}
