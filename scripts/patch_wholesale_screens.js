const fs = require('fs');
const path = require('path');

const wholesaleScreensPath = path.resolve('android/app/src/main/java/com/example/govind/ui/features/wholesale/WholesaleScreens.kt');

const newContent = `package com.example.govind.ui.features.wholesale

import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.govind.data.model.Product
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.shared.PillExperienceSwitcher

@Composable
fun WholesaleHomeScreen(
    viewModel: WholesaleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val openWhatsApp = { message: String ->
        val url = "https://wa.me/919630937033?text=\${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

    Column(modifier = Modifier.fillMaxSize().background(GovindTheme.colors.warmWhite)) {
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
                        painter = androidx.compose.ui.res.painterResource(id = com.example.govind.R.drawable.ic_launcher_squircle),
                        contentDescription = "Govind Logo",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "GOVIND WHOLESALE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GovindTheme.colors.govindGreen)
                }
                Spacer(modifier = Modifier.height(12.dp))
                PillExperienceSwitcher()
            }
        }
        
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.items.isEmpty()) {
            WholesaleEmptyState(
                onRequestQuote = { openWhatsApp("Hi Govind, I would like to request a bulk quote for my business.") },
                onContactGovind = { openWhatsApp("Hi Govind, I am interested in wholesale/bulk orders.") }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GovindTheme.colors.softGreen)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Bulk Mandi Supply & Wholesale Tiers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GovindTheme.colors.govindGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tiered bulk pricing activates automatically in your basket when quantities reach wholesale minimums.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GovindTheme.colors.textSecondary
                            )
                        }
                    }
                }

                items(items = uiState.items) { item ->
                    WholesaleProductRow(item, onAddToCartClick = {
                        viewModel.addToCart(item)
                    })
                }

                item {
                    WholesaleEmptyState(
                        onRequestQuote = { openWhatsApp("Hi Govind, I would like to request a custom bulk quote for my business.") },
                        onContactGovind = { openWhatsApp("Hi Govind, I need custom procurement for my kitchen/restaurant.") }
                    )
                }
            }
        }
    }
}

@Composable
fun WholesaleProductRow(product: Product, onAddToCartClick: () -> Unit) {
    var bestPrice = product.sellingPrice
    var minQty = 1
    
    val wp = product.getWholesalePricing()
    if (wp != null && wp.wholesale_eligible && wp.tiers.isNotEmpty()) {
        val bestTier = wp.tiers.maxByOrNull { it.min_qty }
        if (bestTier != null) {
            minQty = bestTier.min_qty
            if (bestTier.type == "percentage") {
                bestPrice = product.sellingPrice * (1.0 - (bestTier.value / 100.0))
            } else if (bestTier.type == "fixed") {
                bestPrice = bestTier.value
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    Text("📦")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (bestPrice < product.sellingPrice) {
                    Text(
                        text = "From ₹\${String.format(\"%.2f\", bestPrice)} / \${product.unit} (Min \$minQty)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.govindOrange
                    )
                } else {
                    Text(
                        text = "₹\${product.sellingPrice} / \${product.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddToCartClick,
                colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen)
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
fun WholesaleCatalogScreen(
    viewModel: WholesaleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val openWhatsApp = { message: String ->
        val url = "https://wa.me/919630937033?text=\${Uri.encode(message)}"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Column(modifier = Modifier.fillMaxSize().background(GovindTheme.colors.warmWhite)) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "WHOLESALE CATALOG",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.govindGreen
                )
                Text(
                    text = "Direct Mandi & Farm-Sourced Commercial Produce",
                    style = MaterialTheme.typography.bodySmall,
                    color = GovindTheme.colors.textSecondary
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            WholesaleEmptyState(
                onRequestQuote = { openWhatsApp("Hi Govind, I would like to request a bulk quote for my business.") },
                onContactGovind = { openWhatsApp("Hi Govind, I am interested in wholesale/bulk orders.") }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = uiState.items) { item ->
                    WholesaleProductRow(item, onAddToCartClick = {
                        viewModel.addToCart(item)
                    })
                }
            }
        }
    }
}

@Composable
fun WholesaleEmptyState(onRequestQuote: () -> Unit, onContactGovind: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GovindTheme.colors.warmWhite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GovindTheme.colors.softGreen,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Business",
                        tint = GovindTheme.colors.govindGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "COMMERCIAL & BULK ORDERS",
                style = MaterialTheme.typography.labelLarge,
                color = GovindTheme.colors.freshGreen,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Fresh produce for your business.",
                style = MaterialTheme.typography.headlineSmall,
                color = GovindTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Get daily rates and custom bulk quotes for restaurants, caterers, and retail vendors.",
                style = MaterialTheme.typography.bodyLarge,
                color = GovindTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRequestQuote, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request a Bulk Quote", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onContactGovind, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GovindTheme.colors.govindGreen),
                border = androidx.compose.foundation.BorderStroke(1.dp, GovindTheme.colors.govindGreen)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Procurement Team", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
`;

fs.writeFileSync(wholesaleScreensPath, newContent, 'utf8');
console.log('WholesaleScreens.kt updated without fallbacks and with proper currency symbols.');
