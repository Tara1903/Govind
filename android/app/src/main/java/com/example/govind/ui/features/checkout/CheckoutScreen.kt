package com.example.govind.ui.features.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.features.cart.BillRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.orderPlaced) {
        if (uiState.orderPlaced) {
            onNavigateToHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold, color = GovindTheme.colors.textPrimary) },
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
            if (!uiState.isLoading && uiState.cart?.items?.isNotEmpty() == true) {
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
                            onClick = { viewModel.placeOrder() },
                            enabled = uiState.selectedAddressId != null && !uiState.isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 24.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GovindTheme.colors.govindGreen,
                                disabledContainerColor = GovindTheme.colors.skeleton
                            )
                        ) {
                            if (uiState.isLoading && uiState.orderPlaced) {
                                CircularProgressIndicator(color = GovindTheme.colors.pureWhite, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Place Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (uiState.selectedAddressId != null) GovindTheme.colors.pureWhite else GovindTheme.colors.textMuted)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading && !uiState.orderPlaced) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GovindTheme.colors.govindGreen)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}", color = GovindTheme.colors.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(GovindTheme.colors.warmWhite),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    CheckoutSectionTitle("Delivery Address")
                }
                
                items(uiState.addresses, key = { it.id }) { address ->
                    val isSelected = address.id == uiState.selectedAddressId
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectAddress(address.id) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GovindTheme.colors.govindGreen else GovindTheme.colors.border,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) GovindTheme.colors.softGreen else GovindTheme.colors.pureWhite
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) GovindTheme.colors.govindGreen else GovindTheme.colors.textMuted
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = address.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GovindTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${address.house}, ${address.street}, ${address.area}\n${address.city} - ${address.pincode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GovindTheme.colors.textSecondary,
                                    lineHeight = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = address.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = GovindTheme.colors.textPrimary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = GovindTheme.colors.govindGreen
                                )
                            }
                        }
                    }
                }
                
                item {
                    CheckoutSectionTitle("Payment Method")
                }
                
                val paymentMethods = listOf(
                    "ONLINE" to "Pay Online (UPI, Cards, NetBanking)",
                    "COD" to "Cash on Delivery"
                )
                items(paymentMethods) { (method, desc) ->
                    val isSelected = method == uiState.selectedPaymentMethod
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectPaymentMethod(method) }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = GovindTheme.colors.pureWhite
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.selectPaymentMethod(method) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GovindTheme.colors.govindGreen,
                                    unselectedColor = GovindTheme.colors.textMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = GovindTheme.colors.textPrimary
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

                    Spacer(modifier = Modifier.height(8.dp))
                    CheckoutSectionTitle("Order Summary")
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GovindTheme.colors.pureWhite,
                        modifier = Modifier.fillMaxWidth()
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
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun CheckoutSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = GovindTheme.colors.textPrimary
    )
    Spacer(modifier = Modifier.height(16.dp))
}
