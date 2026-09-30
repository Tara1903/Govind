package com.example.govind.ui.features.search

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.Product
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.shared.GovindDiscountBadge
import com.example.govind.ui.shared.GovindPrice
import com.example.govind.ui.shared.GovindVegIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            Surface(
                color = GovindTheme.colors.surface,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GovindTheme.colors.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Stitch Pill Search Bar
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            color = GovindTheme.colors.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = GovindTheme.colors.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextField(
                                    value = uiState.query,
                                    onValueChange = { viewModel.updateQuery(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester),
                                    placeholder = {
                                        Text(
                                            "Search Fresh, Kitchen or Wholesale...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GovindTheme.colors.onSurfaceVariant
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = GovindTheme.colors.onSurface),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                                if (uiState.query.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.updateQuery("") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = GovindTheme.colors.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Experience Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Fresh 🌿", "Kitchen 🍲", "Wholesale 📦").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) GovindTheme.colors.primaryContainer else GovindTheme.colors.surfaceContainer,
                                modifier = Modifier.clickable { selectedFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) GovindTheme.colors.onPrimary else GovindTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = GovindTheme.colors.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.query.isEmpty()) {
                // Trending searches & Recent suggestions
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp,
                            contentDescription = null,
                            tint = GovindTheme.colors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Trending on Govind",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    val trending = listOf(
                        "Farm Malai Paneer", "Punjabi Dal Makhani", "Nashik Onions (50kg)",
                        "Desi Tomatoes", "Amritsari Kulcha", "Organic Spinach",
                        "Butter Garlic Naan", "Shimla Apples"
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trending.forEach { term ->
                            Surface(
                                color = GovindTheme.colors.surfaceContainerLowest,
                                shape = CircleShape,
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable { viewModel.updateQuery(term) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null,
                                        tint = GovindTheme.colors.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = term,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = GovindTheme.colors.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GovindTheme.colors.secondary)
                    }
                } else if (uiState.results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No results for \"${uiState.query}\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GovindTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Try searching for generic terms like Paneer, Dal, Onions, or Tomatoes",
                                style = MaterialTheme.typography.bodySmall,
                                color = GovindTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val filteredResults = uiState.results.filter { product ->
                        when (selectedFilter) {
                            "Fresh 🌿" -> product.experienceType.equals("FRESH", ignoreCase = true)
                            "Kitchen 🍲" -> product.experienceType.equals("KITCHEN", ignoreCase = true)
                            "Wholesale 📦" -> product.experienceType.equals("WHOLESALE", ignoreCase = true)
                            else -> true
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredResults) { product ->
                            SearchResultItem(product = product, onClick = onNavigateToProduct)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(product: Product, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(product.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GovindTheme.colors.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GovindTheme.colors.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val icon = when (product.experienceType) {
                        "KITCHEN" -> "🍲"
                        "WHOLESALE" -> "📦"
                        else -> "🌿"
                    }
                    Text(icon, style = MaterialTheme.typography.headlineMedium)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GovindVegIndicator(isVeg = true, size = 12.dp)
                    Surface(
                        shape = CircleShape,
                        color = when (product.experienceType) {
                            "KITCHEN" -> GovindTheme.colors.tertiaryFixed
                            "WHOLESALE" -> GovindTheme.colors.surfaceContainerHigh
                            else -> GovindTheme.colors.secondaryContainer
                        }
                    ) {
                        Text(
                            text = product.experienceType,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (product.experienceType) {
                                "KITCHEN" -> GovindTheme.colors.tertiaryContainer
                                "WHOLESALE" -> GovindTheme.colors.onSurface
                                else -> GovindTheme.colors.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.onSurface,
                    maxLines = 2
                )
                if (product.unit.isNotBlank()) {
                    Text(
                        text = product.unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GovindPrice(
                        price = product.sellingPrice,
                        mrp = if (product.price > product.sellingPrice) product.price else null
                    )
                    if (product.price > product.sellingPrice) {
                        val discountPct = (((product.price - product.sellingPrice) / product.price) * 100).toInt()
                        if (discountPct > 0) {
                            GovindDiscountBadge(text = "$discountPct% OFF")
                        }
                    }
                }
            }
        }
    }
}
