package com.example.govind.ui.features.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.Address
import com.example.govind.data.model.Order
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.features.cart.BillRow
import com.example.govind.ui.shared.GovindDiscountBadge
import com.example.govind.ui.shared.GovindPrice
import com.example.govind.ui.shared.GovindVegIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToAuth: () -> Unit = {},
    onNavigateToOrderDetails: (String) -> Unit = {},
    onNavigateToStarPay: (amount: Double, orderId: String, orderRef: String, description: String, customerName: String, customerEmail: String, customerPhone: String) -> Unit = { _, _, _, _, _, _, _ -> },
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isAccordionExpanded by remember { mutableStateOf(false) }
    var selectedPaymentTab by remember { mutableStateOf("STARPAY_UPI") }
    var showAddressDialog by remember { mutableStateOf(false) }

    // 1. Dedicated Order Success View
    if (uiState.orderPlaced && uiState.placedOrder != null) {
        OrderSuccessView(
            order = uiState.placedOrder!!,
            onNavigateToHome = onNavigateToHome,
            onNavigateToOrderDetails = onNavigateToOrderDetails
        )
        return
    }

    Scaffold(
        topBar = {
            Surface(
                color = GovindTheme.colors.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GovindTheme.colors.onSurface
                            )
                        }
                        Text(
                            text = "Govind Checkout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.onSurface
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = GovindTheme.colors.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = GovindTheme.colors.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Secure",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GovindTheme.colors.secondary
                            )
                        }
                    }
                }
            }
        },
        containerColor = GovindTheme.colors.surface
    ) { paddingValues ->
        // Phone number collection dialog
        if (uiState.needsPhone && !uiState.phoneSaved) {
            var phoneInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { /* Phone required for logistics */ },
                title = { Text("Phone Number Required", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Please provide your phone number so our delivery partner can reach you for order updates.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GovindTheme.colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Mobile Number (10 digits)") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (phoneInput.isNotBlank()) {
                                viewModel.savePhoneNumber(phoneInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.secondary),
                        enabled = phoneInput.trim().length >= 10
                    ) {
                        Text("Save & Continue", color = GovindTheme.colors.onSecondary)
                    }
                }
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GovindTheme.colors.secondary)
            }
        } else if (!uiState.isAuthenticated) {
            GuestAuthGateView(
                padding = paddingValues,
                onNavigateToAuth = onNavigateToAuth
            )
        } else if (uiState.cart?.items.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧺", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your basket is empty",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add items from Fresh, Kitchen, or Wholesale before proceeding to checkout.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GovindTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToHome,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.primaryContainer)
                    ) {
                        Text("Browse Products", color = GovindTheme.colors.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val cart = uiState.cart!!
            val items = cart.items
            val selectedAddress = uiState.addresses.firstOrNull { it.id == uiState.selectedAddressId }
                ?: uiState.addresses.firstOrNull()

            val subtotal = uiState.itemsSubtotal
            val discount = uiState.totalSavings
            val deliveryFee = uiState.deliveryCharge
            val starPayCashback = 50.0
            val grandTotal = (subtotal + deliveryFee - starPayCashback).coerceAtLeast(0.0)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Error Notice Banner
                    if (uiState.error != null) {
                        item {
                            Surface(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = GovindTheme.colors.error)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = uiState.error!!,
                                        color = GovindTheme.colors.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // 1. Micro Security Assurance Ribbon
                    item {
                        Surface(
                            shape = CircleShape,
                            color = GovindTheme.colors.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = GovindTheme.colors.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "100% Encrypted & Safe Checkout",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GovindTheme.colors.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = GovindTheme.colors.surfaceContainerLowest
                                ) {
                                    Text(
                                        text = "StarPay Secured",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.secondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Delivery Address Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GovindTheme.colors.surfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            if (selectedAddress != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(GovindTheme.colors.surfaceContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = null,
                                                tint = GovindTheme.colors.primaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = selectedAddress.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GovindTheme.colors.onSurface
                                                )
                                                if (selectedAddress.isDefault) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = GovindTheme.colors.secondaryContainer
                                                    ) {
                                                        Text(
                                                            text = "Default",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GovindTheme.colors.onSecondaryContainer,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${selectedAddress.house}, ${selectedAddress.street}, ${selectedAddress.area}, ${selectedAddress.city} - ${selectedAddress.pincode}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = GovindTheme.colors.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Contact: ${selectedAddress.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GovindTheme.colors.onSurface
                                            )
                                        }
                                    }
                                    TextButton(onClick = { showAddressDialog = true }) {
                                        Text(
                                            text = "Change",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.secondary
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToAddAddress() }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = GovindTheme.colors.secondary)
                                        Text(
                                            text = "Add Delivery Address",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.secondary
                                        )
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GovindTheme.colors.secondary)
                                }
                            }
                        }
                    }

                    // 3. Unified Multi-Slot Dispatch Schedule
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GovindTheme.colors.surfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Route,
                                        contentDescription = null,
                                        tint = GovindTheme.colors.primaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Unified Multi-Slot Dispatch",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.onSurface
                                    )
                                }
                                Text(
                                    text = "Contains Fresh Groceries, Hot Kitchen Food & Wholesale Bulk. Optimized for dual logistics:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GovindTheme.colors.onSurfaceVariant
                                )

                                // Slot 1: Instant Express
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GovindTheme.colors.surfaceContainerLow
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(GovindTheme.colors.tertiaryFixed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Bolt,
                                                contentDescription = null,
                                                tint = GovindTheme.colors.tertiaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Slot 1: Instant Express",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GovindTheme.colors.onSurface
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = GovindTheme.colors.tertiaryContainer
                                                ) {
                                                    Text(
                                                        text = "15–20 Mins",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GovindTheme.colors.onTertiary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Fresh Produce + Hot Kitchen Food dispatched together in insulated thermal carriers.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GovindTheme.colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Slot 2: Wholesale Freight
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GovindTheme.colors.surfaceContainerLow
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(GovindTheme.colors.secondaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocalShipping,
                                                contentDescription = null,
                                                tint = GovindTheme.colors.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Slot 2: Wholesale Freight",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GovindTheme.colors.onSurface
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = GovindTheme.colors.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "Tomorrow 06:30 AM",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GovindTheme.colors.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Bulk sacks/crates via heavy freight commercial dock trolley at your building gate.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GovindTheme.colors.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Order Summary Accordion
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GovindTheme.colors.surfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isAccordionExpanded = !isAccordionExpanded }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBasket,
                                            contentDescription = null,
                                            tint = GovindTheme.colors.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Order Summary",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = GovindTheme.colors.onSurface
                                            )
                                            Text(
                                                text = "${items.size} items from Fresh, Kitchen & Wholesale",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = GovindTheme.colors.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isAccordionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = GovindTheme.colors.onSurface
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isAccordionExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh)
                                        items.forEach { cartItem ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    GovindVegIndicator(isVeg = true, size = 12.dp)
                                                    Column {
                                                        Text(
                                                            text = cartItem.product.name,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GovindTheme.colors.onSurface
                                                        )
                                                        Text(
                                                            text = "${cartItem.quantity} × ₹${cartItem.product.sellingPrice.toInt()}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = GovindTheme.colors.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "₹${(cartItem.product.sellingPrice * cartItem.quantity).toInt()}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GovindTheme.colors.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. StarPay Payment Gateway Selector
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GovindTheme.colors.surfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = GovindTheme.colors.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Payment Method",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.onSurface
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = GovindTheme.colors.secondaryContainer
                                    ) {
                                        Text(
                                            text = "₹50 CASHBACK",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Option 1: StarPay UPI (Selected)
                                PaymentOptionCard(
                                    title = "StarPay UPI & One-Click Pay",
                                    subtitle = "priyasharma@starpay (Primary)",
                                    badge = "FASTER",
                                    note = "Zero failure rate • Instant refund guarantee • Auto ₹50 off",
                                    icon = Icons.Default.Stars,
                                    isSelected = selectedPaymentTab == "STARPAY_UPI",
                                    onClick = {
                                        selectedPaymentTab = "STARPAY_UPI"
                                        viewModel.selectPaymentMethod("STARPAY_UPI")
                                    }
                                )

                                // Option 2: StarPay Wallet / Pay Later
                                PaymentOptionCard(
                                    title = "StarPay Wallet / Pay Later",
                                    subtitle = "Vyapar Credit Pre-approved: ₹25,000",
                                    badge = "B2B READY",
                                    note = "Single consolidated monthly settlement",
                                    icon = Icons.Default.CreditScore,
                                    isSelected = selectedPaymentTab == "WALLET",
                                    onClick = {
                                        selectedPaymentTab = "WALLET"
                                        viewModel.selectPaymentMethod("COD")
                                    }
                                )

                                // Option 3: Cards
                                PaymentOptionCard(
                                    title = "Cards (RuPay, Visa, Mastercard)",
                                    subtitle = "StarPay Tokenized Vault Security",
                                    badge = null,
                                    note = null,
                                    icon = Icons.Default.CreditCard,
                                    isSelected = selectedPaymentTab == "CARDS",
                                    onClick = {
                                        selectedPaymentTab = "CARDS"
                                        viewModel.selectPaymentMethod("COD")
                                    }
                                )

                                // Option 4: Cash on Delivery
                                PaymentOptionCard(
                                    title = "Cash / Pay on Delivery",
                                    subtitle = "Pay upon receiving delivery",
                                    badge = null,
                                    note = null,
                                    icon = Icons.Default.Payments,
                                    isSelected = selectedPaymentTab == "COD",
                                    onClick = {
                                        selectedPaymentTab = "COD"
                                        viewModel.selectPaymentMethod("COD")
                                    }
                                )
                            }
                        }
                    }

                    // 6. Billing Breakdown Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GovindTheme.colors.surfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Bill Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GovindTheme.colors.onSurface
                                )
                                BillRow("Items Total Subtotal", "₹${subtotal.toInt()}")
                                if (discount > 0) {
                                    BillRow("Direct Mandi & Combo Discount", "-₹${discount.toInt()}", color = GovindTheme.colors.secondary)
                                }
                                BillRow("StarPay Instant Cashback", "-₹50", color = GovindTheme.colors.secondary)
                                BillRow(
                                    label = "Logistics & Handling",
                                    value = if (deliveryFee == 0.0) "FREE" else "₹${deliveryFee.toInt()}",
                                    color = if (deliveryFee == 0.0) GovindTheme.colors.secondary else GovindTheme.colors.onSurface
                                )

                                HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "To Pay",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.onSurface
                                        )
                                        Text(
                                            text = "Total Savings: ₹${(discount + 50).toInt()} on this order",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GovindTheme.colors.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "₹${grandTotal.toInt()}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GovindTheme.colors.primaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // 7. Policy Micro Note
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = GovindTheme.colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Farm pure guarantee • No questions asked cancellation before dispatch",
                                style = MaterialTheme.typography.bodySmall,
                                color = GovindTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }

                // 8. Sticky Bottom CTA Bar
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = GovindTheme.colors.surface.copy(alpha = 0.95f),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "₹${grandTotal.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GovindTheme.colors.onSurface
                                )
                                Text(
                                    text = "• ${items.sumOf { it.quantity }} Items",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GovindTheme.colors.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = GovindTheme.colors.secondary
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Savings,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Saved ₹${(discount + 50).toInt()} Total",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.placeOrder(onNavigateToStarPay = onNavigateToStarPay)
                            },
                            enabled = selectedAddress != null && !uiState.isPlacingOrder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GovindTheme.colors.primaryContainer,
                                disabledContainerColor = GovindTheme.colors.surfaceContainerHigh
                            )
                        ) {
                            if (uiState.isPlacingOrder) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = GovindTheme.colors.primaryFixed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (selectedPaymentTab == "STARPAY_UPI") {
                                            "Pay with StarPay — ₹${grandTotal.toInt()}"
                                        } else {
                                            "Place Order (COD) — ₹${grandTotal.toInt()}"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = "By continuing, you accept StarPay and GOVIND terms",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Address Switcher Dialog
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Select Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.addresses.forEach { addr ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (addr.id == uiState.selectedAddressId) GovindTheme.colors.secondaryContainer.copy(alpha = 0.4f) else GovindTheme.colors.surfaceContainerLowest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectAddress(addr.id)
                                    showAddressDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(addr.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text("${addr.house}, ${addr.street}, ${addr.area}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddressDialog = false
                    onNavigateToAddAddress()
                }) {
                    Text("+ Add New Address", fontWeight = FontWeight.Bold, color = GovindTheme.colors.secondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddressDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PaymentOptionCard(
    title: String,
    subtitle: String,
    badge: String?,
    note: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) GovindTheme.colors.surfaceContainerLow else GovindTheme.colors.surfaceContainerLowest,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GovindTheme.colors.secondary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) GovindTheme.colors.secondary else GovindTheme.colors.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                            if (badge != null) {
                                Surface(shape = RoundedCornerShape(4.dp), color = GovindTheme.colors.primaryFixed) {
                                    Text(badge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onPrimaryFixed, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                        }
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (isSelected) GovindTheme.colors.secondary else GovindTheme.colors.onSurfaceVariant, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
                Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) GovindTheme.colors.secondary else GovindTheme.colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            if (note != null) {
                Text(note, style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant, modifier = Modifier.padding(start = 30.dp))
            }
        }
    }
}

@Composable
fun GuestAuthGateView(
    padding: PaddingValues,
    onNavigateToAuth: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GovindTheme.colors.surfaceContainerLowest,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GovindTheme.colors.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Authentication Required",
                        tint = GovindTheme.colors.secondary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Sign In to Checkout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your unified basket is saved. Sign in or register to select your delivery address and complete checkout.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GovindTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToAuth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.primaryContainer)
                ) {
                    Text(
                        "Sign In / Register",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSuccessView(
    order: Order,
    onNavigateToHome: () -> Unit,
    onNavigateToOrderDetails: (String) -> Unit
) {
    Scaffold(
        containerColor = GovindTheme.colors.surface,
        bottomBar = {
            Surface(
                color = GovindTheme.colors.surfaceContainerLowest,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigateToOrderDetails(order.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.primaryContainer)
                    ) {
                        Text(
                            "View Live Tracking",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    OutlinedButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GovindTheme.colors.primaryContainer)
                    ) {
                        Text(
                            "Continue Shopping",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.primaryContainer
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GovindTheme.colors.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = GovindTheme.colors.secondary,
                        modifier = Modifier.size(52.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Order Placed Successfully!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = GovindTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order #${order.id.take(8).uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.secondary
                )
                Text(
                    text = "Full Ref: ${order.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GovindTheme.colors.onSurfaceVariant
                )
            }

            // Status Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GovindTheme.colors.surfaceContainerLowest,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STATUS", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = GovindTheme.colors.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    order.orderStatus,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GovindTheme.colors.secondary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PAYMENT", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = GovindTheme.colors.primaryFixed, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "STARPAY / COD",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GovindTheme.colors.onPrimaryFixed
                                )
                            }
                        }
                    }
                }
            }

            // Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GovindTheme.colors.surfaceContainerLowest,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Order Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.onSurface
                        )
                        BillRow("Items Subtotal", "₹${order.subtotal}")
                        if (order.savings > 0) {
                            BillRow("Total Savings", "-₹${order.savings}", color = GovindTheme.colors.secondary)
                        }
                        BillRow("Delivery Charge", if (order.deliveryCharge == 0.0) "FREE" else "₹${order.deliveryCharge}")
                        HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            Text("₹${order.total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = GovindTheme.colors.primaryContainer)
                        }
                    }
                }
            }
        }
    }
}
