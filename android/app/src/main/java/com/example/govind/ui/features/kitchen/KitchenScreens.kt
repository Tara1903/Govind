package com.example.govind.ui.features.kitchen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.Product
import com.example.govind.theme.Dimens
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.shared.*

// ═════════════════════════════════════════════════════════════
// KITCHEN HOME SCREEN — STITCH DESIGN SPECIFICATION
// ═════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenHomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val totalCartCount = uiState.cart?.items?.sumOf { it.quantity } ?: 0

    Scaffold(
        topBar = {
            GovindTopBar(
                locationName = "Sector 48, Gurugram",
                eta = "25m",
                cartItemCount = totalCartCount,
                onLocationClick = {},
                onCartClick = onNavigateToCart,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.menuItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GovindTheme.colors.kitchenAccent)
            }
        } else if (uiState.error != null && uiState.menuItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Failed to load kitchen menu", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Experience Switcher
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        PillExperienceSwitcher()
                    }
                }

                // 2. Live Kitchen Status & Kitchen ETA Notice
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        LiveKitchenStatusCard()
                    }
                }

                // 3. Unified Multi-Cart Insight Banner
                if (totalCartCount > 0) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 4.dp)) {
                            SharedCartInsightBanner(totalItems = totalCartCount)
                        }
                    }
                }

                // 4. Search Bar
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        KitchenSearchBar(onSearchClick = onNavigateToMenu)
                    }
                }

                // 5. Promotional Hero Banner: Dhabe Di Khushboo
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        KitchenHeroBanner(onClaimClick = onNavigateToMenu)
                    }
                }

                // 6. Category Filter Tabs
                item {
                    KitchenCategoryFilterTabs(
                        activeCategory = uiState.activeCategory,
                        onCategorySelected = { viewModel.setActiveCategory(it) }
                    )
                }

                // 7. Chef's Specials & Bestsellers Heading
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.Margin, vertical = Dimens.SpaceSm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GovindTheme.colors.kitchenAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Chef's Specials & Bestsellers",
                                style = MaterialTheme.typography.titleMedium,
                                color = GovindTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${uiState.filteredItems.size} Dishes",
                            style = MaterialTheme.typography.labelMedium,
                            color = GovindTheme.colors.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 8. Food Items Grid (2 Columns)
                val items = uiState.filteredItems
                val rowCount = (items.size + 1) / 2
                for (rowIndex in 0 until rowCount) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.Margin, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val leftIndex = rowIndex * 2
                            val rightIndex = leftIndex + 1

                            if (leftIndex < items.size) {
                                val item = items[leftIndex]
                                val qty = uiState.cartQuantities[item.id] ?: 0
                                Box(modifier = Modifier.weight(1f)) {
                                    KitchenFoodCard(
                                        product = item,
                                        quantityInCart = qty,
                                        onAddToCart = { viewModel.addToCart(item) },
                                        onIncrement = { viewModel.updateQuantity(item, qty + 1) },
                                        onDecrement = { viewModel.updateQuantity(item, qty - 1) }
                                    )
                                }
                            }

                            if (rightIndex < items.size) {
                                val item = items[rightIndex]
                                val qty = uiState.cartQuantities[item.id] ?: 0
                                Box(modifier = Modifier.weight(1f)) {
                                    KitchenFoodCard(
                                        product = item,
                                        quantityInCart = qty,
                                        onAddToCart = { viewModel.addToCart(item) },
                                        onIncrement = { viewModel.updateQuantity(item, qty + 1) },
                                        onDecrement = { viewModel.updateQuantity(item, qty - 1) }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// KITCHEN FULL MENU SCREEN
// ═════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenMenuScreen(
    viewModel: KitchenViewModel = hiltViewModel()
) {
    KitchenHomeScreen(
        onNavigateToCart = {},
        viewModel = viewModel
    )
}

// ═════════════════════════════════════════════════════════════
// KITCHEN COMPONENTS
// ═════════════════════════════════════════════════════════════

@Composable
fun LiveKitchenStatusCard() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GovindTheme.colors.kitchenTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OutdoorGrill,
                        contentDescription = null,
                        tint = GovindTheme.colors.kitchenAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Govind Kitchen #4",
                            style = MaterialTheme.typography.titleSmall,
                            color = GovindTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = GovindTheme.colors.kitchenTint,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "LIVE TANDOOR",
                                style = MaterialTheme.typography.labelSmall,
                                color = GovindTheme.colors.kitchenAccent,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Express Kitchen Serving Sector 48, Gurugram",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = GovindTheme.colors.kitchenAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "25 MINS",
                        style = MaterialTheme.typography.labelMedium,
                        color = GovindTheme.colors.kitchenAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Hot & Fresh",
                    style = MaterialTheme.typography.labelSmall,
                    color = GovindTheme.colors.textMuted
                )
            }
        }
    }
}

@Composable
fun SharedCartInsightBanner(totalItems: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(GovindTheme.colors.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = totalItems.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Shared Cart: Fresh groceries + Hot meal ready",
                    style = MaterialTheme.typography.bodySmall,
                    color = GovindTheme.colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "1 Single Delivery",
                style = MaterialTheme.typography.labelSmall,
                color = GovindTheme.colors.secondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KitchenSearchBar(onSearchClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSearchClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = GovindTheme.colors.kitchenAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Search dishes, authentic thalis, combos...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = "Voice",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun KitchenHeroBanner(onClaimClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GovindTheme.colors.brandPrimary,
                            Color(0xFF5A1500),
                            GovindTheme.colors.kitchenAccent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = GovindTheme.colors.kitchenTint,
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = GovindTheme.colors.kitchenAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "GRAND FEAST WEEK",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.kitchenAccent,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Column {
                    Text(
                        text = "Dhabe Di Khushboo",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Slow-cooked Punjabi Dal Makhani, Paneer Tikka & Amritsari Thalis crafted fresh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.kitchenTint
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Save ₹100 Instant",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "On Combos above ₹349 • Code: DHABA100",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = GovindTheme.colors.kitchenTint
                            )
                        }

                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.clickable(onClick = onClaimClick)
                        ) {
                            Text(
                                text = "Claim",
                                color = GovindTheme.colors.kitchenAccent,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KitchenCategoryFilterTabs(
    activeCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "All Dishes",
        "Breakfast",
        "Punjabi Specials",
        "Deluxe Thalis",
        "Combos",
        "Snacks & Chaat",
        "Beverages",
        "Desserts"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.Margin),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == activeCategory
            val bgColor = if (isSelected) GovindTheme.colors.kitchenAccent else MaterialTheme.colorScheme.surfaceContainerLowest
            val textColor = if (isSelected) Color.White else GovindTheme.colors.textPrimary

            Surface(
                color = bgColor,
                shape = CircleShape,
                shadowElevation = if (isSelected) 2.dp else 1.dp,
                modifier = Modifier.clickable { onCategorySelected(category) }
            ) {
                Text(
                    text = category,
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun KitchenFoodCard(
    product: Product,
    quantityInCart: Int,
    onAddToCart: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🍛", fontSize = 42.sp)
                    }
                }

                // FSSAI Veg marker top left
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    GovindVegIndicator(
                        isVeg = true,
                        size = 12.dp,
                        modifier = Modifier.padding(2.dp)
                    )
                }

                // Rating pill top right
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "4.8",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.textPrimary
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = GovindTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = product.description ?: product.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = GovindTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (quantityInCart > 0) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "₹${(product.sellingPrice * quantityInCart).toInt()}",
                                style = GovindTheme.priceDisplay,
                                color = GovindTheme.colors.brandPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "$quantityInCart in cart • ₹${product.sellingPrice.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = GovindTheme.colors.secondary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Column {
                            if (product.price != null && product.price > product.sellingPrice) {
                                Text(
                                    text = "₹${product.price.toInt()}",
                                    style = GovindTheme.priceStrikethrough,
                                    color = GovindTheme.colors.textMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                            Text(
                                text = "₹${product.sellingPrice.toInt()}",
                                style = GovindTheme.priceDisplay,
                                color = GovindTheme.colors.textPrimary
                            )
                        }
                    }

                    if (quantityInCart <= 0) {
                        Surface(
                            color = GovindTheme.colors.kitchenTint,
                            shape = CircleShape,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 64.dp, minHeight = 30.dp)
                                .clickable(onClick = onAddToCart)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ADD",
                                    color = GovindTheme.colors.kitchenAccent,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = GovindTheme.colors.kitchenAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    } else {
                        GovindQuantityControl(
                            quantity = quantityInCart,
                            onIncrement = onIncrement,
                            onDecrement = onDecrement
                        )
                    }
                }
            }
        }
    }
}
