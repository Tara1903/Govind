package com.example.govind.ui.features.wholesale

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
// WHOLESALE HOME SCREEN — STITCH SPECIFICATION
// ═════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesaleHomeScreen(
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRateList: () -> Unit = {},
    onNavigateToProduct: (String) -> Unit = {},
    viewModel: WholesaleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val totalCartCount = uiState.cartQuantities.values.sum()

    val openWhatsApp = { message: String ->
        val url = "https://wa.me/919630937033?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            GovindTopBar(
                locationName = "Sector 48, Gurugram",
                eta = "B2B",
                cartItemCount = totalCartCount,
                onLocationClick = {},
                onCartClick = onNavigateToCart,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primaryContainer)
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

                // 2. B2B Wholesale Notice Banner & Mandi Ticker
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        B2BMandiNoticeBanner()
                    }
                }

                // 3. Live Market Stats Row
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 4.dp)) {
                        LiveMarketStatsRow()
                    }
                }

                // 4. Search & Scan Bar
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        WholesaleSearchBar(onSearchClick = onNavigateToSearch)
                    }
                }

                // 5. Commercial Farm Sourcing Hero Banner
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        WholesaleHeroBanner(
                            onApplyOrder = {
                                openWhatsApp("Hi Govind, I want to use code BULK300 for commercial farm sourcing.")
                            }
                        )
                    }
                }

                // 6. Quick Wholesale Categories (4x2 Bento Grid)
                item {
                    WholesaleCategoryGrid(
                        onCategoryClick = { category ->
                            if (category.contains("Rate List", ignoreCase = true)) {
                                onNavigateToRateList()
                            } else {
                                onNavigateToSearch()
                            }
                        }
                    )
                }

                // 7. High-Volume Mandi Arrivals (Tiered Pricing Cards)
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
                                imageVector = Icons.Outlined.LocalFireDepartment,
                                contentDescription = null,
                                tint = GovindTheme.colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "High-Volume Mandi Arrivals",
                                style = MaterialTheme.typography.titleMedium,
                                color = GovindTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Updated 10m ago",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.textMuted
                        )
                    }
                }

                // Wholesale Product Cards with Tier Pricing Matrix
                val displayItems = if (uiState.items.isNotEmpty()) {
                    uiState.items
                } else {
                    getSampleWholesaleProducts()
                }

                items(displayItems) { product ->
                    val qty = uiState.cartQuantities[product.id] ?: 0
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        WholesaleTierProductCard(
                            product = product,
                            quantityInCart = qty,
                            onAddToCart = { viewModel.addToCart(product, 1) },
                            onIncrement = { viewModel.updateQuantity(product, qty + 1) },
                            onDecrement = { viewModel.updateQuantity(product, qty - 1) }
                        )
                    }
                }

                // 8. GOVIND Vyapar Credit Banner
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 8.dp)) {
                        VyaparCreditBanner(
                            onApply = { openWhatsApp("Hi Govind, I would like to apply for GOVIND Vyapar Credit 15-day line.") }
                        )
                    }
                }

                // 9. Trust Markers (3 columns)
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        WholesaleTrustMarkers()
                    }
                }

                // 10. WhatsApp & Call for Bulk Rates
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 8.dp)) {
                        WholesaleContactCard(
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919630937033"))
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                openWhatsApp("Hi Govind, I need a bulk mandi quote for my business.")
                            }
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// WHOLESALE CATALOG SCREEN
// ═════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesaleCatalogScreen(
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRateList: () -> Unit = {},
    onNavigateToProduct: (String) -> Unit = {},
    viewModel: WholesaleViewModel = hiltViewModel()
) {
    WholesaleHomeScreen(
        onNavigateToCart = onNavigateToCart,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToRateList = onNavigateToRateList,
        onNavigateToProduct = onNavigateToProduct,
        viewModel = viewModel
    )
}


// ═════════════════════════════════════════════════════════════
// SUBCOMPONENTS
// ═════════════════════════════════════════════════════════════

@Composable
fun B2BMandiNoticeBanner() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Bolt,
                    contentDescription = null,
                    tint = GovindTheme.colors.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "B2B Mandi Hub: GST Invoice • Free Dock Delivery > ₹2,500",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null,
                tint = GovindTheme.colors.secondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LiveMarketStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = GovindTheme.colors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Mandi Index",
                        style = MaterialTheme.typography.labelSmall,
                        color = GovindTheme.colors.textMuted
                    )
                    Text(
                        text = "↓ 4.2% APMC Rates",
                        style = MaterialTheme.typography.labelMedium,
                        color = GovindTheme.colors.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Dock Slot",
                        style = MaterialTheme.typography.labelSmall,
                        color = GovindTheme.colors.textMuted
                    )
                    Text(
                        text = "Tomorrow 06:00 AM",
                        style = MaterialTheme.typography.labelMedium,
                        color = GovindTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WholesaleSearchBar(onSearchClick: () -> Unit = {}) {
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
                tint = GovindTheme.colors.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Search 500+ bulk staples, crates, oils...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Outlined.QrCodeScanner,
                contentDescription = "Scan",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
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
fun WholesaleHeroBanner(onApplyOrder: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
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
                            Color(0xFF0F3B20),
                            GovindTheme.colors.secondary
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GovindTheme.colors.secondary,
                        shape = CircleShape
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warehouse,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "MANDI FESTIVAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                    Text(
                        text = "Up to 38% Extra Margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Commercial Farm Sourcing",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Nashik Onions, Kolar Tomatoes, MP Sharbati Wheat direct from verified agricultural collection centers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    )
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ConfirmationNumber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "BULK300",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "₹300 off on ₹5,000+",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.clickable(onClick = onApplyOrder)
                        ) {
                            Text(
                                text = "Apply & Order",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WholesaleCategoryGrid(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        Triple("Farm Crates", "📦", "Veggies"),
        Triple("Dal & Pulses", "🌾", "50kg Sack"),
        Triple("Atta & Rice", "🍚", "Commercial"),
        Triple("Oils & Ghee", "🫒", "15L Tin"),
        Triple("Spices Bulk", "🌶️", "Whole & Powder"),
        Triple("Dairy & Paneer", "🧀", "Catering Blocks"),
        Triple("Packaging", "🥡", "Takeaway"),
        Triple("Today's Rate List", "📊", "Live APMC")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        GovindSectionHeader(
            title = "Mandi Wholesale Categories",
            subtitle = "Bulk orders direct from agricultural mandis",
            icon = Icons.Outlined.Category,
            onSeeAllClick = {}
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Margin),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (row in 0 until (categories.size + 3) / 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 4) {
                        val index = row * 4 + col
                        if (index < categories.size) {
                            val (title, emoji, hint) = categories[index]
                            Box(modifier = Modifier.weight(1f)) {
                                GovindCategoryCircle(
                                    title = title,
                                    priceHint = hint,
                                    iconEmoji = emoji,
                                    onClick = { onCategoryClick(title) }
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

@Composable
fun WholesaleTierProductCard(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Section: Image + Title + Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    if (!product.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    } else {
                        Text("📦", fontSize = 36.sp)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "MOQ: ${product.unit}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = GovindTheme.colors.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Mandi Direct Sourced",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = GovindTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = product.description ?: "APMC Grade-A sorted & weighed",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "₹${product.sellingPrice.toInt()}",
                            style = GovindTheme.priceDisplay,
                            color = GovindTheme.colors.textPrimary
                        )
                        if (product.price != null && product.price > product.sellingPrice) {
                            Text(
                                text = "₹${product.price.toInt()}",
                                style = GovindTheme.priceStrikethrough,
                                color = GovindTheme.colors.textMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }
            }

            // Tier Pricing Matrix Box per Stitch
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tier Volume (MOQ)",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.textMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "B2B Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.textMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Savings",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.textMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tier 1 (Baseline)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tier 1 (1 - 3 Units)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "₹${product.sellingPrice.toInt()}/unit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Baseline",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tier 2 (Bulk)
                    val tier2Price = (product.sellingPrice * 0.85).toInt()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tier 2 (4+ Units)",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.textPrimary
                        )
                        Text(
                            text = "₹$tier2Price/unit",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.secondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Save 15%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Action Row: Stepper or ADD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (quantityInCart > 0) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "₹${(quantityInCart * product.sellingPrice).toInt()}",
                            style = GovindTheme.priceDisplay,
                            color = GovindTheme.colors.brandPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$quantityInCart in basket • ₹${product.sellingPrice.toInt()}/${product.unit}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = GovindTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "₹${product.sellingPrice.toInt()}",
                            style = GovindTheme.priceDisplay,
                            color = GovindTheme.colors.textPrimary
                        )
                        Text(
                            text = "per ${product.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GovindTheme.colors.textMuted
                        )
                    }
                }

                if (quantityInCart <= 0) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add • ₹${product.sellingPrice.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
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

@Composable
fun VyaparCreditBanner(onApply: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(16.dp),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "GOVIND Vyapar Credit",
                        style = MaterialTheme.typography.titleSmall,
                        color = GovindTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "15-Day 0% Interest Commercial Line",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.textMuted
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.clickable(onClick = onApply)
            ) {
                Text(
                    text = "Apply Now",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun WholesaleTrustMarkers() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrustItemMini(Icons.Outlined.Verified, "Sortex Verified", "Mandi Optical Sort", Modifier.weight(1f))
        TrustItemMini(Icons.Outlined.AssignmentReturn, "Gate Rejection", "100% Replacement", Modifier.weight(1f))
        TrustItemMini(Icons.Outlined.ReceiptLong, "GST ITC Invoice", "Direct Credit Filing", Modifier.weight(1f))
    }
}

@Composable
fun TrustItemMini(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GovindTheme.colors.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = GovindTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                color = GovindTheme.colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WholesaleContactCard(
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "COMMERCIAL & BULK PROCUREMENT",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primaryContainer,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Supplying hotels, caterers, cloud kitchens, and restaurants across Gurugram with direct farm pricing.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = GovindTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Desk")
                }
                Button(
                    onClick = onWhatsApp,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun getSampleWholesaleProducts(): List<Product> {
    return listOf(
        Product(
            id = "w1",
            name = "Nashik Grade-A Red Onions (50kg Sack)",
            slug = "nashik-grade-a-red-onions-50kg",
            sellingPrice = 1200.0,
            price = 1750.0,
            unit = "50kg Sack",
            description = "Size: 45mm - 55mm • Moisture-tested Nashik harvest",
            categoryId = "wholesale-veg"
        ),
        Product(
            id = "w2",
            name = "Fresh Malai Paneer Commercial Blocks (10kg)",
            slug = "fresh-malai-paneer-10kg",
            sellingPrice = 2900.0,
            price = 4200.0,
            unit = "10kg Tub",
            description = "Restaurant soft-grade pure buffalo milk paneer",
            categoryId = "wholesale-dairy"
        ),
        Product(
            id = "w3",
            name = "Kolar Commercial Red Tomatoes (25kg Crate)",
            slug = "kolar-commercial-red-tomatoes-25kg",
            sellingPrice = 650.0,
            price = 900.0,
            unit = "25kg Crate",
            description = "Firm shelf-stable round hybrid tomatoes",
            categoryId = "wholesale-veg"
        )
    )
}
