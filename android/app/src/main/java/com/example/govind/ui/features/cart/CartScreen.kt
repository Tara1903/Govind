package com.example.govind.ui.features.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.CartItem
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.shared.GovindQuantityControl

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
                title = { Text("Your Basket", fontWeight = FontWeight.Bold, color = GovindTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GovindTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GovindTheme.colors.warmWhite)
            )
        },
        containerColor = GovindTheme.colors.warmWhite,
        bottomBar = {
            if (!uiState.isLoading && !uiState.cart?.items.isNullOrEmpty()) {
                Surface(
                    color = GovindTheme.colors.pureWhite,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val subtotal = uiState.cart!!.items.sumOf { (it.product.price ?: it.product.sellingPrice) * it.quantity }
                        val sellingTotal = uiState.cart!!.items.sumOf { it.product.sellingPrice * it.quantity }
                        val discount = subtotal - sellingTotal
                        val delivery = if (sellingTotal > 150) 0.0 else 40.0
                        val toPay = sellingTotal + delivery
                        
                        Column {
                            Text(
                                text = "\u20B9${toPay}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = GovindTheme.colors.govindGreen
                            )
                            if (discount > 0) {
                                Text(
                                    text = "Saved \u20B9${discount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GovindTheme.colors.govindOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToCheckout,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 24.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen)
                        ) {
                            Text("Checkout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.pureWhite)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GovindTheme.colors.govindGreen)
            }
        } else if (uiState.cart?.items.isNullOrEmpty()) {
            EmptyCartState(onNavigateBack, paddingValues)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(uiState.cart!!.items, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrement = { viewModel.increaseQuantity(item.id, item.quantity) },
                        onDecrement = { viewModel.decreaseQuantity(item.id, item.quantity) }
                    )
                }

                item {
                    val items = uiState.cart!!.items
                    val sellingTotal = items.sumOf { it.product.sellingPrice * it.quantity }
                    if (sellingTotal <= 150) {
                        val needed = 150.0 - sellingTotal
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = GovindTheme.colors.softOrange,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Add \u20B9$needed more for FREE Delivery",
                                color = GovindTheme.colors.govindOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = GovindTheme.colors.softFresh,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Yay! You got FREE Delivery",
                                color = GovindTheme.colors.freshGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    val items = uiState.cart!!.items
                    val subtotal = items.sumOf { (it.product.price ?: it.product.sellingPrice) * it.quantity }
                    val sellingTotal = items.sumOf { it.product.sellingPrice * it.quantity }
                    val discount = subtotal - sellingTotal
                    val delivery = if (sellingTotal > 150) 0.0 else 40.0
                    val grandTotal = sellingTotal + delivery

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Order Summary", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.ExtraBold,
                        color = GovindTheme.colors.textPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GovindTheme.colors.pureWhite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            BillRow("Items total", "\u20B9$subtotal")
                            Spacer(modifier = Modifier.height(12.dp))
                            if (discount > 0) {
                                BillRow("Savings", "-\u20B9$discount", color = GovindTheme.colors.govindOrange)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            BillRow(
                                label = "Delivery Charge", 
                                value = if (delivery == 0.0) "FREE" else "\u20B9$delivery", 
                                color = if (delivery == 0.0) GovindTheme.colors.freshGreen else GovindTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = GovindTheme.colors.border)
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = GovindTheme.colors.textPrimary)
                                Text("\u20B9$grandTotal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = GovindTheme.colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartState(onNavigateBack: () -> Unit, paddingValues: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            // Using a subtle surface instead of giant illustration
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = GovindTheme.colors.softGreen,
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🥬", style = MaterialTheme.typography.displayLarge)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Your basket is waiting for something fresh.",
                style = MaterialTheme.typography.titleMedium, 
                color = GovindTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen),
                modifier = Modifier.height(56.dp).fillMaxWidth()
            ) {
                Text("Continue Shopping", fontWeight = FontWeight.Bold, color = GovindTheme.colors.pureWhite)
            }
        }
    }
}

@Composable
fun BillRow(label: String, value: String, color: Color = GovindTheme.colors.textSecondary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = GovindTheme.colors.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = GovindTheme.colors.pureWhite
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GovindTheme.colors.skeleton)
            ) {
                if (item.product.imageUrl != null) {
                    AsyncImage(
                        model = item.product.imageUrl,
                        contentDescription = item.product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.textPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.product.unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = GovindTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\u20B9${item.product.sellingPrice}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = GovindTheme.colors.textPrimary
                    )
                    if (item.product.price != null && item.product.price > item.product.sellingPrice) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\u20B9${item.product.price}",
                            style = MaterialTheme.typography.labelMedium.copy(textDecoration = TextDecoration.LineThrough),
                            color = GovindTheme.colors.textMuted
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                GovindQuantityControl(
                    quantity = item.quantity,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    isCartTheme = false
                )
            }
        }
    }
}
