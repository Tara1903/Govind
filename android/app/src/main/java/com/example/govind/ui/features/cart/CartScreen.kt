package com.example.govind.ui.features.cart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Loyalty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.CartItem
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.shared.GovindDiscountBadge
import com.example.govind.ui.shared.GovindPrice
import com.example.govind.ui.shared.GovindVegIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val experienceType by com.example.govind.ui.navigation.AppState.currentExperience.collectAsStateWithLifecycle()

    LaunchedEffect(experienceType) {
        viewModel.loadCart()
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
                        Column {
                            Text(
                                text = "Shared Cart",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GovindTheme.colors.onSurface
                            )
                            val itemCount = uiState.cart?.items?.sumOf { it.quantity } ?: 0
                            Text(
                                text = if (itemCount > 0) "$itemCount items across Fresh, Kitchen & Wholesale" else "Basket empty",
                                style = MaterialTheme.typography.bodySmall,
                                color = GovindTheme.colors.onSurfaceVariant
                            )
                        }
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
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = GovindTheme.colors.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "UNIFIED",
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GovindTheme.colors.secondary)
            }
        } else if (uiState.cart?.items.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🛒", style = MaterialTheme.typography.displayLarge)
                    Text(
                        text = "Your Unified Basket is Empty",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.onSurface
                    )
                    Text(
                        text = "Explore Farm Fresh Produce, Hot Tandoori Kitchen, or Mandi Wholesale and combine them in one shared delivery.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GovindTheme.colors.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = onNavigateBack,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.primaryContainer)
                    ) {
                        Text(
                            text = "Start Shopping",
                            color = GovindTheme.colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            val items = uiState.cart!!.items
            val freshItems = items.filter { it.experienceType.equals("FRESH", ignoreCase = true) }
            val kitchenItems = items.filter { it.experienceType.equals("KITCHEN", ignoreCase = true) }
            val wholesaleItems = items.filter { it.experienceType.equals("WHOLESALE", ignoreCase = true) }

            val subtotal = uiState.totalAmount
            val discount = uiState.totalSavings
            val isFreeDelivery = subtotal >= 500.0
            val deliveryFee = if (isFreeDelivery) 0.0 else 40.0
            val couponDiscount = 100.0
            val mandiHandling = if (wholesaleItems.isNotEmpty()) 38.0 else 0.0
            val grandTotal = (subtotal + deliveryFee + mandiHandling - couponDiscount).coerceAtLeast(0.0)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Active Delivery Location Pill
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GovindTheme.colors.surfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
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
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(GovindTheme.colors.secondaryContainer.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = null,
                                            tint = GovindTheme.colors.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Delivering to Sector 48, Gurugram",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.onSurface
                                        )
                                        Text(
                                            text = "Split Multi-Fleet Delivery • 12 to 15 mins",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GovindTheme.colors.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GovindTheme.colors.surfaceContainer
                                ) {
                                    Text(
                                        text = "Change",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Gamified Free Delivery Threshold Bar
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = GovindTheme.colors.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Instant Delivery Threshold",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.onSurface
                                        )
                                    }
                                    val amountAway = (500.0 - subtotal).coerceAtLeast(0.0)
                                    Surface(
                                        shape = CircleShape,
                                        color = GovindTheme.colors.secondaryContainer
                                    ) {
                                        Text(
                                            text = if (amountAway == 0.0) "FREE DELIVERY UNLOCKED" else "₹${amountAway.toInt()} away from FREE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.secondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                val progress = (subtotal / 500.0).toFloat().coerceIn(0f, 1f)
                                val animatedProgress by animateFloatAsState(targetValue = progress, label = "deliveryProgress")
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = GovindTheme.colors.secondary,
                                    trackColor = GovindTheme.colors.surfaceContainerHigh
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Current subtotal: ₹${subtotal.toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GovindTheme.colors.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Goal: ₹500",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 3. Pillar 1: Govind Fresh
                    if (freshItems.isNotEmpty()) {
                        item {
                            CartPillarCard(
                                title = "GOVIND FRESH",
                                iconEmoji = "🌿",
                                badgeText = "${freshItems.size} items",
                                badgeColor = GovindTheme.colors.surfaceContainer,
                                badgeTextColor = GovindTheme.colors.onSurfaceVariant,
                                items = freshItems,
                                headerBg = GovindTheme.colors.surfaceContainerLow,
                                onIncrease = { item -> viewModel.increaseQuantity(item.id, item.quantity, item.experienceType) },
                                onDecrease = { item -> viewModel.decreaseQuantity(item.id, item.quantity, item.experienceType) },
                                etaText = "Harvested 5 AM • 12 mins"
                            )
                        }
                    }

                    // 4. Pillar 2: Govind Kitchen
                    if (kitchenItems.isNotEmpty()) {
                        item {
                            CartPillarCard(
                                title = "GOVIND KITCHEN",
                                iconEmoji = "🍲",
                                badgeText = "${kitchenItems.size} items",
                                badgeColor = GovindTheme.colors.surfaceContainer,
                                badgeTextColor = GovindTheme.colors.onSurfaceVariant,
                                items = kitchenItems,
                                headerBg = GovindTheme.colors.tertiaryFixed.copy(alpha = 0.35f),
                                onIncrease = { item -> viewModel.increaseQuantity(item.id, item.quantity, item.experienceType) },
                                onDecrease = { item -> viewModel.decreaseQuantity(item.id, item.quantity, item.experienceType) },
                                etaText = "Express Cloud Kitchen • Hot dispatch"
                            )
                        }
                    }

                    // 5. Pillar 3: Govind Wholesale
                    if (wholesaleItems.isNotEmpty()) {
                        item {
                            CartPillarCard(
                                title = "GOVIND WHOLESALE",
                                iconEmoji = "📦",
                                badgeText = "MANDI B2B",
                                badgeColor = GovindTheme.colors.secondaryContainer,
                                badgeTextColor = GovindTheme.colors.onSecondaryContainer,
                                items = wholesaleItems,
                                headerBg = GovindTheme.colors.surfaceContainer,
                                onIncrease = { item -> viewModel.increaseQuantity(item.id, item.quantity, item.experienceType) },
                                onDecrease = { item -> viewModel.decreaseQuantity(item.id, item.quantity, item.experienceType) },
                                etaText = "Dock Slot: 6:30 AM Tomorrow",
                                isWholesale = true
                            )
                        }
                    }

                    // 6. Frequently Added Together Carousel
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
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
                                        imageVector = Icons.Outlined.Celebration,
                                        contentDescription = null,
                                        tint = GovindTheme.colors.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Frequently Added Together",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.onSurface
                                    )
                                }
                                Text(
                                    text = "QUICK ADD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GovindTheme.colors.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    QuickAddCard(
                                        title = "Desi Coriander",
                                        unit = "100g bunch",
                                        tag = "FRESH HARVEST",
                                        price = 15.0,
                                        tagColor = GovindTheme.colors.secondary
                                    )
                                }
                                item {
                                    QuickAddCard(
                                        title = "Butter Garlic Naan",
                                        unit = "1 pc • Tandoori",
                                        tag = "HOT BREAD",
                                        price = 45.0,
                                        tagColor = GovindTheme.colors.tertiaryContainer
                                    )
                                }
                                item {
                                    QuickAddCard(
                                        title = "Agro Crate Liner",
                                        unit = "Pack of 10",
                                        tag = "WHOLESALE ACC.",
                                        price = 60.0,
                                        tagColor = GovindTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // 7. Coupon Widget
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GovindTheme.colors.surfaceContainerLowest,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(GovindTheme.colors.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Loyalty,
                                            contentDescription = null,
                                            tint = GovindTheme.colors.onSecondaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "GOVINDTRIO",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = GovindTheme.colors.onSurface
                                            )
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = GovindTheme.colors.secondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = "Extra ₹100 combo discount applied",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GovindTheme.colors.secondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GovindTheme.colors.surfaceContainer
                                ) {
                                    Text(
                                        text = "Change",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 8. Commercial Breakdown & Bill Details
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
                                    text = "Bill Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GovindTheme.colors.onSurface
                                )

                                BillRow(label = "Items Subtotal", value = "₹${subtotal.toInt()}")
                                if (discount > 0) {
                                    BillRow(label = "Product Catalog Discounts", value = "-₹${discount.toInt()}", color = GovindTheme.colors.secondary)
                                }
                                BillRow(label = "Coupon GOVINDTRIO", value = "-₹100", color = GovindTheme.colors.secondary)
                                BillRow(
                                    label = "Delivery Fee",
                                    value = if (isFreeDelivery) "FREE" else "₹40",
                                    color = if (isFreeDelivery) GovindTheme.colors.secondary else GovindTheme.colors.onSurface
                                )
                                if (wholesaleItems.isNotEmpty()) {
                                    BillRow(label = "Mandi Handling & GST", value = "₹38")
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = GovindTheme.colors.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Celebration,
                                            contentDescription = null,
                                            tint = GovindTheme.colors.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "🎉 You are saving ₹${(discount + 100).toInt()} on this unified cart!",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.onSecondaryContainer
                                        )
                                    }
                                }

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
                                            text = "Inclusive of all taxes & mandi levies",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GovindTheme.colors.onSurfaceVariant
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

                    // 9. Govind Direct Purity Promise
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GovindTheme.colors.surfaceContainerHigh.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = GovindTheme.colors.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Govind Direct Purity Promise",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.onSurface
                                    )
                                    Text(
                                        text = "100% replacement guarantee if perishable quality is compromised. Seamless single-invoice billing with separate logistics tracking for Farm, Hot Food and Wholesale.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GovindTheme.colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 10. Sticky Bottom Floating Checkout Dock
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = GovindTheme.colors.primaryContainer,
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "₹${grandTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "(${items.sumOf { it.quantity }} items)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GovindTheme.colors.secondaryContainer
                                )
                            }
                            Text(
                                text = "View Split Details",
                                style = MaterialTheme.typography.labelSmall,
                                color = GovindTheme.colors.secondaryContainer,
                                textDecoration = TextDecoration.Underline
                            )
                        }

                        Button(
                            onClick = onNavigateToCheckout,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.secondary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Proceed to Checkout",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GovindTheme.colors.onSecondary
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = GovindTheme.colors.onSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartPillarCard(
    title: String,
    iconEmoji: String,
    badgeText: String,
    badgeColor: Color,
    badgeTextColor: Color,
    items: List<CartItem>,
    headerBg: Color,
    onIncrease: (CartItem) -> Unit,
    onDecrease: (CartItem) -> Unit,
    etaText: String,
    isWholesale: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GovindTheme.colors.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(iconEmoji, fontSize = 16.sp)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.onSurface
                    )
                    Surface(shape = CircleShape, color = badgeColor) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                val subtotal = items.sumOf { it.product.sellingPrice * it.quantity }
                Text(
                    text = "₹${subtotal.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = GovindTheme.colors.primaryContainer
                )
            }

            // Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items.forEach { cartItem ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GovindTheme.colors.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!cartItem.product.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = cartItem.product.imageUrl,
                                    contentDescription = cartItem.product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(iconEmoji, style = MaterialTheme.typography.titleLarge)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cartItem.product.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GovindTheme.colors.onSurface,
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f)
                                )
                                GovindVegIndicator(isVeg = true, size = 12.dp)
                            }
                            if (cartItem.product.unit.isNotBlank()) {
                                Text(
                                    text = cartItem.product.unit,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GovindTheme.colors.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GovindPrice(
                                    price = cartItem.product.sellingPrice,
                                    mrp = if (cartItem.product.price > cartItem.product.sellingPrice) cartItem.product.price else null
                                )
                                if (cartItem.product.price > cartItem.product.sellingPrice) {
                                    val savings = (cartItem.product.price - cartItem.product.sellingPrice).toInt()
                                    GovindDiscountBadge(text = "Save ₹$savings")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = etaText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GovindTheme.colors.secondary,
                                    fontWeight = FontWeight.Medium
                                )

                                // Stitch Quantity Stepper Pill
                                Surface(
                                    shape = CircleShape,
                                    color = if (isWholesale) GovindTheme.colors.surfaceContainerHigh else GovindTheme.colors.primaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onDecrease(cartItem) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                tint = if (isWholesale) GovindTheme.colors.onSurface else GovindTheme.colors.onPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = "${cartItem.quantity}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWholesale) GovindTheme.colors.onSurface else GovindTheme.colors.onPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { onIncrease(cartItem) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = if (isWholesale) GovindTheme.colors.onSurface else GovindTheme.colors.onPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAddCard(
    title: String,
    unit: String,
    tag: String,
    price: Double,
    tagColor: Color
) {
    Surface(
        modifier = Modifier.width(135.dp),
        shape = RoundedCornerShape(14.dp),
        color = GovindTheme.colors.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GovindTheme.colors.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("🌿", fontSize = 24.sp)
            }
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tagColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GovindTheme.colors.onSurface,
                maxLines = 1
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = GovindTheme.colors.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${price.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.onSurface
                )
                Surface(
                    shape = CircleShape,
                    color = GovindTheme.colors.primaryContainer,
                    modifier = Modifier.clickable { /* quick add */ }
                ) {
                    Text(
                        text = "+ Add",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BillRow(
    label: String,
    value: String,
    color: Color = GovindTheme.colors.onSurfaceVariant
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = GovindTheme.colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
