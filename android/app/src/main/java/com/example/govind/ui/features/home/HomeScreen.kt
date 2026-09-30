package com.example.govind.ui.features.home

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
import com.example.govind.data.model.Category
import com.example.govind.data.model.Product
import com.example.govind.theme.Dimens
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.shared.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRateList: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All Fresh") }

    val totalCartItems = uiState.cartQuantities.values.sum()

    Scaffold(
        topBar = {
            GovindTopBar(
                locationName = "Sector 48, Gurugram",
                eta = "12m",
                cartItemCount = totalCartItems,
                onLocationClick = {},
                onCartClick = onNavigateToCart,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.freshBoardProducts.isEmpty() && uiState.featuredProducts.isEmpty()) {
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

                // 2. Search Bar (Stitch pill style with Voice + Scan)
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        FreshSearchBar(
                            onSearchClick = onNavigateToSearch,
                            onVoiceClick = onNavigateToSearch,
                            onScanClick = onNavigateToSearch
                        )
                    }
                }

                // 3. Express Filter Pills
                item {
                    ExpressFilterPills(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                }

                // 4. Hero Freshness Ticker Banner
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        HeroFreshnessTicker()
                    }
                }

                // 5. Hero Product Banner
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        HeroPromoBanner(onExploreClick = onNavigateToSearch)
                    }
                }

                // 6. Category Grid (4x2 items)
                item {
                    FreshCategoryGrid(
                        categories = uiState.categories,
                        onCategoryClick = { onNavigateToSearch() },
                        onSeeAllClick = onNavigateToSearch
                    )
                }

                // 7. Daily Harvest Picks Carousel
                val harvestProducts = if (uiState.freshBoardProducts.isNotEmpty()) {
                    uiState.freshBoardProducts
                } else {
                    uiState.featuredProducts
                }

                if (harvestProducts.isNotEmpty()) {
                    item {
                        GovindSectionHeader(
                            title = "Daily Harvest Picks",
                            subtitle = "Direct Mandi Arrival • Harvested 5:00 AM",
                            icon = Icons.Outlined.Spa,
                            onSeeAllClick = onNavigateToSearch
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Dimens.Margin),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(harvestProducts) { product ->
                                val qtyInCart = uiState.cartQuantities[product.id] ?: 0
                                GovindProductCard(
                                    product = product,
                                    quantityInCart = qtyInCart,
                                    onClick = { onNavigateToProduct(product.id) },
                                    onAddClick = { viewModel.addToCart(product, 1) },
                                    onIncrement = { viewModel.updateQuantity(product, qtyInCart + 1) },
                                    onDecrement = { viewModel.updateQuantity(product, qtyInCart - 1) },
                                    modifier = Modifier.width(164.dp)
                                )
                            }
                        }
                    }
                }

                // 8. Deals of the Day (Under ₹49)
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 8.dp)) {
                        DealsOfTheDayCard(
                            onAddDeal = { name, price ->
                                val dealProduct = harvestProducts.firstOrNull { it.name.contains(name, ignoreCase = true) }
                                if (dealProduct != null) {
                                    viewModel.addToCart(dealProduct, 1)
                                } else {
                                    onNavigateToSearch()
                                }
                            }
                        )
                    }
                }

                // 9. Daily Breakfast Basket Bundle
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 8.dp)) {
                        DailyBreakfastBasketCard(
                            onAddBasket = {
                                if (uiState.combos.isNotEmpty()) {
                                    viewModel.addToCart(uiState.combos.first(), 1)
                                } else if (uiState.packs.isNotEmpty()) {
                                    viewModel.addToCart(uiState.packs.first(), 1)
                                } else {
                                    onNavigateToSearch()
                                }
                            }
                        )
                    }
                }

                // 10. Farm Direct Assurance Micro-Banner
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        FarmDirectAssuranceBanner()
                    }
                }

                // 11. Mandi Daily Rates Shortcut
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        MandiRateCard(onRateListClick = onNavigateToRateList)
                    }
                }

                // 12. WhatsApp Daily Rate List & Wholesale Call
                item {
                    Box(modifier = Modifier.padding(horizontal = Dimens.Margin, vertical = 6.dp)) {
                        WhatsAppDailyRateList(context = context)
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// SUBCOMPONENTS
// ═════════════════════════════════════════════════════════════

@Composable
fun FreshSearchBar(
    onSearchClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
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
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Search vegetables, fruits, dairy...",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = "Voice Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onScanClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpressFilterPills(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf(
        Pair("All Fresh", Icons.Outlined.CheckCircle),
        Pair("Under 15 Mins", Icons.Outlined.Bolt),
        Pair("100% Organic", Icons.Outlined.Eco),
        Pair("Steal Deals", Icons.Outlined.LocalOffer)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.Margin),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items(filters) { (title, icon) ->
            val isSelected = title == selectedFilter
            val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest
            val contentColor = if (isSelected) Color.White else GovindTheme.colors.textPrimary

            Surface(
                color = bgColor,
                shape = CircleShape,
                shadowElevation = if (isSelected) 2.dp else 1.dp,
                modifier = Modifier.clickable { onFilterSelected(title) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else GovindTheme.colors.secondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun HeroFreshnessTicker() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, GovindTheme.colors.secondary.copy(alpha = 0.25f)),
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
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GovindTheme.colors.secondary)
                )
                Text(
                    text = "HARVESTED TODAY AT 5:00 AM",
                    style = MaterialTheme.typography.labelSmall,
                    color = GovindTheme.colors.textPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "• Mandi to Doorstep",
                    style = MaterialTheme.typography.bodySmall,
                    color = GovindTheme.colors.textMuted
                )
            }

            GovindDeliveryETA(eta = "12-min delivery")
        }
    }
}

@Composable
fun HeroPromoBanner(onExploreClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onExploreClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GovindTheme.colors.brandPrimary,
                            GovindTheme.colors.primaryContainer,
                            GovindTheme.colors.secondary
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "☀️ Mandi Harvest 5 AM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = GovindTheme.colors.kitchenTint,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "⚡ 10-15m",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.kitchenAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Fresh Alphonso & Hydroponics",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Direct from Ratnagiri & local polyhouses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Code: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = GovindTheme.colors.kitchenAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "FRESH25",
                                style = MaterialTheme.typography.labelMedium,
                                color = GovindTheme.colors.textPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "25% OFF",
                                style = MaterialTheme.typography.labelSmall,
                                color = GovindTheme.colors.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = GovindTheme.colors.secondary,
                        shape = CircleShape
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Explore",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FreshCategoryGrid(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    val defaultCategories = listOf(
        Triple("Fresh Veggies", "from ₹18", "🥬"),
        Triple("Farm Fruits", "from ₹45", "🍎"),
        Triple("Leafy Herbs", "Morning Dew", "🌿"),
        Triple("Exotics", "Air-Flown", "🥑"),
        Triple("A2 Dairy", "Pure Churned", "🥛"),
        Triple("Oils & Ghee", "Wood Pressed", "🫒"),
        Triple("Daily Staples", "Chakki Fresh", "🌾"),
        Triple("Dry Fruits", "Handpicked", "🥜")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        GovindSectionHeader(
            title = "Explore Farm Categories",
            subtitle = "Farm handpicked daily at dawn",
            icon = Icons.Outlined.Spa,
            onSeeAllClick = onSeeAllClick
        )

        val itemsToDisplay = if (categories.isNotEmpty()) {
            categories.take(8).mapIndexed { index, cat ->
                val fallback = defaultCategories.getOrElse(index) { Triple(cat.name, "Farm Fresh", "🥬") }
                Triple(cat.name, fallback.second, fallback.third)
            }
        } else {
            defaultCategories
        }

        // 4 columns x 2 rows grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Margin),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in 0 until (itemsToDisplay.size + 3) / 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0 until 4) {
                        val index = row * 4 + col
                        if (index < itemsToDisplay.size) {
                            val (title, hint, emoji) = itemsToDisplay[index]
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
fun DealsOfTheDayCard(
    onAddDeal: (String, Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GovindTheme.colors.kitchenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = GovindTheme.colors.kitchenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Deals of the Day Under ₹49",
                            style = MaterialTheme.typography.titleSmall,
                            color = GovindTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Farm fresh items at wholesale rates",
                            style = MaterialTheme.typography.bodySmall,
                            color = GovindTheme.colors.textMuted
                        )
                    }
                }

                Surface(
                    color = GovindTheme.colors.kitchenAccent,
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "02:27:36",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 mini deal items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DealMiniItem(
                    title = "Yellow Lemons",
                    unit = "250 g",
                    price = 19,
                    emoji = "🍋",
                    onAdd = { onAddDeal("Yellow Lemons", 19) },
                    modifier = Modifier.weight(1f)
                )
                DealMiniItem(
                    title = "Red Carrots",
                    unit = "500 g",
                    price = 29,
                    emoji = "🥕",
                    onAdd = { onAddDeal("Red Carrots", 29) },
                    modifier = Modifier.weight(1f)
                )
                DealMiniItem(
                    title = "Button Mushroom",
                    unit = "200 g",
                    price = 42,
                    emoji = "🍄",
                    onAdd = { onAddDeal("Button Mushroom", 42) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DealMiniItem(
    title: String,
    unit: String,
    price: Int,
    emoji: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "₹$price",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Text(
                    text = emoji,
                    fontSize = 28.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = GovindTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = GovindTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = CircleShape,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAdd)
            ) {
                Text(
                    text = "+ ADD",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DailyBreakfastBasketCard(
    onAddBasket: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Daily Breakfast Basket",
                            style = MaterialTheme.typography.titleSmall,
                            color = GovindTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Handcrafted morning combo",
                            style = MaterialTheme.typography.bodySmall,
                            color = GovindTheme.colors.textMuted
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "SAVE ₹48",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Items row with '+' signs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BundleItemMini("🥛", "A2 Milk")
                Text("+", color = GovindTheme.colors.textMuted, fontWeight = FontWeight.Bold)
                BundleItemMini("🍞", "Wheat Bread")
                Text("+", color = GovindTheme.colors.textMuted, fontWeight = FontWeight.Bold)
                BundleItemMini("🥚", "Eggs (6)")
                Text("+", color = GovindTheme.colors.textMuted, fontWeight = FontWeight.Bold)
                BundleItemMini("🍌", "Bananas 500g")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "₹182",
                            style = GovindTheme.priceDisplay,
                            color = GovindTheme.colors.textPrimary
                        )
                        Text(
                            text = "₹230",
                            style = GovindTheme.priceStrikethrough,
                            color = GovindTheme.colors.textMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Text(
                        text = "All 4 morning essentials included",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.secondary
                    )
                }

                Button(
                    onClick = onAddBasket,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Basket (₹182)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BundleItemMini(emoji: String, name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = GovindTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FarmDirectAssuranceBanner() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = GovindTheme.colors.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Zero Chemical Farm Gate Check",
                        style = MaterialTheme.typography.titleSmall,
                        color = GovindTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Triple-washed, ozone treated, untouched produce",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.textMuted
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GovindTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MandiRateCard(onRateListClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRateListClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TODAY'S FRESH RATES",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Live Mandi & Wholesale Prices Updated Daily",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = "View Rates",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun WhatsAppDailyRateList(context: Context) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val url = "https://wa.me/919630937033?text=Hi,%20I%20want%20today's%20fresh%20rate%20list."
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFE8F5E9)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF25D366)),
                contentAlignment = Alignment.Center
            ) {
                Text("WA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WHATSAPP DAILY RATE LIST",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF128C7E)
                )
                Text(
                    text = "Send HI on WhatsApp to get daily fresh mandi prices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}
