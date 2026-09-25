package com.example.govind.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.Category
import com.example.govind.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HomeTopBar(onSearchClick = onNavigateToSearch)
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    PromotionalBanner()
                }

                item {
                    CategoriesSection(
                        categories = uiState.categories,
                        onCategoryClick = { /* Navigate to category */ }
                    )
                }

                item {
                    SectionHeader(title = "Featured Products", subtitle = "Handpicked for you")
                }

                item {
                    FeaturedProductsSection(
                        products = uiState.featuredProducts,
                        onProductClick = onNavigateToProduct,
                        onAddToCartClick = { product -> viewModel.addToCart(product, 1) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(onSearchClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Delivery in 10 minutes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Home - Flat 101, Main Street...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onSearchClick() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search for 'mangoes'",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PromotionalBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image would go here
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = "Fresh Harvest",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Up to 30% OFF",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit
) {
    Column {
        SectionHeader(title = "Shop by Category")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .clickable { onCategoryClick(category.id) }
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(80.dp)
                    ) {
                        AsyncImage(
                            model = category.image,
                            contentDescription = category.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FeaturedProductsSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onAddToCartClick: (Product) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product.id) },
                onAddClick = { onAddToCartClick(product) },
                modifier = Modifier.width(160.dp)
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                
                // Discount Badge
                if (product.price != null && product.price > product.sellingPrice) {
                    val discount = ((product.price - product.sellingPrice) / product.price * 100).toInt()
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "$discount% OFF",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.price != null && product.price > product.sellingPrice) {
                            Text(
                                text = "₹${product.price}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${product.sellingPrice}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Button(
                        onClick = onAddClick,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .width(64.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("ADD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
