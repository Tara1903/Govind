package com.example.govind.ui.features.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.cart?.items?.isNotEmpty() == true) {
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
                                text = "₹${uiState.totalAmount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "TOTAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = onNavigateToCheckout,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 24.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Proceed to Pay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        } else if (uiState.cart?.items.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your cart is empty", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Start Shopping")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Delivery in 10 minutes", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(uiState.cart!!.items) { item ->
                    CartItemCard(
                        item = item,
                        onIncrement = { viewModel.increaseQuantity(item.id, item.quantity) },
                        onDecrement = { viewModel.decreaseQuantity(item.id, item.quantity) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bill Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            BillRow("Item Total", "₹${uiState.totalAmount}")
                            Spacer(modifier = Modifier.height(8.dp))
                            BillRow("Delivery Fee", "₹40.0")
                            Spacer(modifier = Modifier.height(8.dp))
                            BillRow("Handling Fee", "₹5.0")
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("To Pay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("₹${uiState.totalAmount + 45.0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text = item.product.unit,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${item.product.sellingPrice}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(24.dp)
            ) {
                // If it's a minus sign, normally we use a custom icon, here using a text or close for 1
                Text("-", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Text(
                text = item.quantity.toString(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
