package com.example.govind.ui.features.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
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
    val context = LocalContext.current

    Scaffold(
        topBar = {
            HomeTopBar(onSearchClick = onNavigateToSearch)
        },
        containerColor = com.example.govind.theme.GovindTheme.colors.warmWhite
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = "Error: " + uiState.error + "", color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item { TodaysFreshRatePromotion(products = uiState.freshBoardProducts, onProductClick = onNavigateToProduct) }
                item { FourCoreCategories(categories = uiState.categories, onCategoryClick = { /* TODO filter by category */ }) }
                item { SuperSaverKitchenPack() }
                item { DailyFreshSpecialCombo() }
                item {
                    SectionHeader(title = "TODAY'S PUNJABI MENU", subtitle = "Authentic & Ready-to-Eat")
                    FeaturedProductsSection(
                        products = uiState.featuredProducts,
                        onProductClick = onNavigateToProduct,
                        onAddToCartClick = { product, qty -> viewModel.addToCart(product, qty) }
                    )
                }
                item { WholesaleBulkOrders(context) }
                item { WhatsAppDailyRateList(context) }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun HomeTopBar(onSearchClick: () -> Unit) {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.govind.R.drawable.ic_launcher_squircle),
                        contentDescription = "Govind Logo",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    com.example.govind.ui.shared.ExperienceSwitcherDropdown()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { onSearchClick() },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                        text = "Search fresh produce or Punjabi food...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun TodaysFreshRatePromotion(products: List<com.example.govind.data.model.Product>, onProductClick: (String) -> Unit) {
    if (products.isEmpty()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF064520)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "TODAY AT GOVIND",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFEFCF5),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            products.forEach { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product.id) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "\u20B9${product.sellingPrice}/${product.unit}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Updated 10:42 AM",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFEFCF5).copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun FourCoreCategories(categories: List<com.example.govind.data.model.Category>, onCategoryClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
        SectionHeader(title = "Shop by Category")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.size(100.dp).clickable { onCategoryClick(category.id) },
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = category.name.replace(" ", "\n"),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuperSaverKitchenPack() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "SUPER SAVER KITCHEN PACK",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Essential Veggies Combo - Onion, Potato, Tomato (3 kg total)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to Cart - ?99", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DailyFreshSpecialCombo() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "DAILY FRESH SPECIAL COMBO",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dal Makhani + 2 Butter Naan + Paneer Sabzi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add to Cart - ?249", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WholesaleBulkOrders(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WHOLESALE / BULK ORDERS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Planning a party or need bulk veggies for your restaurant?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:+919630937033")
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Phone, contentDescription = "Call")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call for Bulk Rate", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WhatsAppDailyRateList(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                val url = "https://wa.me/919630937033?text=Hi,%20I%20want%20today's%20fresh%20rate%20list."
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF25D366), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("WA", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "WHATSAPP DAILY RATE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF128C7E)
                )
                Text(
                    text = "Send HI to get daily fresh prices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF128C7E).copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
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
    onAddToCartClick: (Product, Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product.id) },
                onAddClick = { qty -> onAddToCartClick(product, qty) },
                modifier = Modifier.width(160.dp)
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Image", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                if (product.price != null && product.price > product.sellingPrice) {
                    val discount = ((product.price - product.sellingPrice) / product.price * 100).toInt()
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(bottomEnd = 12.dp, topStart = 20.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "$discount% OFF",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.price != null && product.price > product.sellingPrice) {
                            Text(
                                text = "?" + product.price,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "?" + product.sellingPrice,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    
                    AnimatedContent(
                        targetState = quantity > 0,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        }, label = "qty"
                    ) { hasQuantity ->
                        if (hasQuantity) {
                            com.example.govind.ui.shared.GovindQuantityControl(
                                quantity = quantity,
                                onIncrement = { 
                                    quantity++
                                    onAddClick(1) 
                                },
                                onDecrement = { 
                                    quantity--
                                    onAddClick(-1) 
                                },
                                isCartTheme = false
                            )
                        } else {
                            Button(
                                onClick = { 
                                    quantity = 1
                                    onAddClick(1)
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(64.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.example.govind.theme.GovindTheme.colors.softGreen,
                                    contentColor = com.example.govind.theme.GovindTheme.colors.govindGreen
                                )
                            ) {
                                Text("ADD", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}




