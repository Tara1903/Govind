const fs = require('fs');
const path = require('path');

const kitchenScreensPath = path.resolve('android/app/src/main/java/com/example/govind/ui/features/kitchen/KitchenScreens.kt');

const newContent = `package com.example.govind.ui.features.kitchen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.govind.data.model.Product
import coil3.compose.AsyncImage
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.shared.PillExperienceSwitcher

val kitchenAccentColor = Color(0xFFF5450D)

// ─── Shared Top Bar ────────────────────────────────────────────────────────────
@Composable
fun KitchenTopBar() {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(
                        id = com.example.govind.R.drawable.ic_launcher_squircle
                    ),
                    contentDescription = "Govind Logo",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "GOVIND KITCHEN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = kitchenAccentColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            PillExperienceSwitcher()
        }
    }
}

// ─── Home Screen ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenHomeScreen(
    onNavigateToCart: () -> Unit,
    onNavigateToMenu: () -> Unit = {},
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val chips = listOf("All", "Meals", "Paratha", "Thali", "Sides")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GovindTheme.colors.softCream)
    ) {
        KitchenTopBar()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // ── Hero ──────────────────────────────────────────
            Text("Order fresh & healthy", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Authentic Punjabi Food & Home-Style Meals",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GovindTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KitchenMetaChip(icon = Icons.Default.LocationOn, text = "Indore")
                KitchenMetaChip(icon = Icons.Default.Star, text = "4.9 • Fresh Daily")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // ── Cart summary promo card ──────────────────────
            val cartCount = uiState.cart?.items?.filter { it.experienceType == "KITCHEN" }?.sumOf { it.quantity } ?: 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GovindTheme.colors.warmWhite),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kitchen Basket", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (cartCount > 0) "$cartCount item\${if (cartCount > 1) "s" else ""} in your kitchen cart"
                        else "Hot & Fresh Punjabi Meals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Order Dal Makhani, Paneer Thali, Stuffed Parathas and more.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GovindTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = if (cartCount > 0) onNavigateToCart else onNavigateToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = kitchenAccentColor),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (cartCount > 0) "View Cart" else "Browse Full Menu")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Quick Filters ─────────────────────────────────
            Text("Categories", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
            Text("Find your favorite dishes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chips) { chip ->
                    val isSelected = chip == uiState.activeCategory
                    Surface(
                        color = if (isSelected) kitchenAccentColor else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { viewModel.setActiveCategory(chip) }
                    ) {
                        Text(
                            chip,
                            color = if (isSelected) Color.White else GovindTheme.colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // ── Menu Items ────────────────────────────────────
            Text("Quick order", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
            Text("Today's Popular Kitchen Dishes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = kitchenAccentColor)
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Couldn't load menu: \${uiState.error}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.retry() }, colors = ButtonDefaults.buttonColors(containerColor = kitchenAccentColor)) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
                uiState.filteredItems.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        uiState.filteredItems.take(8).forEach { product ->
                            ProductMenuCard(product = product, onAddToCart = { viewModel.addToCart(product) })
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No dishes found for '\${uiState.activeCategory}'.",
                            color = GovindTheme.colors.textSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Category shortcuts ────────────────────────────────
            Text("Popular Sections", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
            Text("Tap to explore", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            val categoryItems = listOf("Meals" to "🍛", "Paratha" to "🥙", "Thali" to "🍱", "Sides" to "🥣")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categoryItems) { (name, emoji) ->
                    Card(
                        modifier = Modifier.size(100.dp).clickable { viewModel.setActiveCategory(name) },
                        colors = CardDefaults.cardColors(containerColor = GovindTheme.colors.warmWhite),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(emoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Menu Screen (full browse) ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenMenuScreen(
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val chips = listOf("All", "Meals", "Paratha", "Thali", "Sides")
    val priceFilters = listOf("All", "Under ₹100", "₹100 - ₹300", "Above ₹300")

    Column(modifier = Modifier.fillMaxSize().background(GovindTheme.colors.softCream)) {
        KitchenTopBar()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text("Full Menu", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Fresh Punjabi Dishes & Daily Meals",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GovindTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Toolbar card
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = GovindTheme.colors.warmWhite), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Categories", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips) { chip ->
                            val isSelected = chip == uiState.activeCategory
                            Surface(
                                color = if (isSelected) kitchenAccentColor else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.clickable { viewModel.setActiveCategory(chip) }
                            ) {
                                Text(chip, color = if (isSelected) Color.White else GovindTheme.colors.textPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Price Range", style = MaterialTheme.typography.labelMedium, color = GovindTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(priceFilters) { filter ->
                            val isSelected = filter == uiState.activePriceFilter
                            Surface(
                                color = if (isSelected) kitchenAccentColor else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.clickable { viewModel.setActivePriceFilter(filter) }
                            ) {
                                Text(filter, color = if (isSelected) Color.White else GovindTheme.colors.textPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Dishes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = kitchenAccentColor)
                    }
                }
                uiState.filteredItems.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        uiState.filteredItems.forEach { product ->
                            ProductMenuCard(product = product, onAddToCart = { viewModel.addToCart(product) })
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No dishes match these filters", color = GovindTheme.colors.textSecondary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Card components ───────────────────────────────────────────────────────────

@Composable
fun ProductMenuCard(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GovindTheme.colors.pureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text("🍲", fontSize = 40.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (!product.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(product.description, style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.textSecondary, maxLines = 2)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹\${product.sellingPrice.toInt()}", style = MaterialTheme.typography.titleMedium, color = kitchenAccentColor, fontWeight = FontWeight.Bold)
                    if (product.price != null && product.price > product.sellingPrice) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("₹\${product.price.toInt()}", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("/ \${product.unit}", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.textSecondary)
                }
            }
            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.softGreen, contentColor = GovindTheme.colors.govindGreen),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(width = 64.dp, height = 36.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ADD", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun KitchenMetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = GovindTheme.colors.textSecondary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.textSecondary)
        }
    }
}
`;

fs.writeFileSync(kitchenScreensPath, newContent, 'utf8');
console.log('KitchenScreens.kt updated without fallbacks.');
