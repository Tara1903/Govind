package com.example.govind.ui.features.orders

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.data.model.Order
import com.example.govind.theme.GovindTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetails(orderId)
    }

    val order = uiState.selectedOrder ?: uiState.orders.find { it.id == orderId }

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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GovindTheme.colors.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GovindTheme.colors.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Govind Express",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.secondary
                        )
                        Text(
                            text = "Order #${orderId.take(8).uppercase(Locale.ROOT)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.onSurface
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = GovindTheme.colors.surfaceContainerHigh,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919999999999?text=Hi%20Govind%20Support,%20I%20need%20help%20with%20Order%20$orderId"))
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = GovindTheme.colors.primaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Support",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GovindTheme.colors.onSurface
                            )
                        }
                    }
                }
            }
        },
        containerColor = GovindTheme.colors.surface
    ) { padding ->
        if (order == null && (uiState.isSelectedOrderLoading || uiState.isLoading)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GovindTheme.colors.secondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Massive ETA Display Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = GovindTheme.colors.surfaceContainerLowest,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = EaseInOut),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "dotAlpha"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(GovindTheme.colors.secondary.copy(alpha = alpha))
                                    )
                                    Text(
                                        text = "On Time • Live GPS Tracking",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.secondary
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = GovindTheme.colors.tertiaryFixed
                                ) {
                                    Text(
                                        text = "Express 12m",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GovindTheme.colors.onTertiaryFixed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "18 MINS",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GovindTheme.colors.primaryContainer
                                )
                                Text(
                                    text = "estimated arrival",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GovindTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Text(
                                text = "Rider is 2.4 km away from Sector 48, Gurugram • Updated just now",
                                style = MaterialTheme.typography.bodySmall,
                                color = GovindTheme.colors.onSurfaceVariant
                            )

                            // Multi-Experience Tag
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GovindTheme.colors.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(GovindTheme.colors.secondaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Eco, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(14.dp))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(GovindTheme.colors.tertiaryFixed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = GovindTheme.colors.tertiaryContainer, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Shared Delivery: Fresh Farm Produce + Hot Kitchen Meal",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.onSurface
                                        )
                                        Text(
                                            text = "Packed in thermal-isolated dual pods",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GovindTheme.colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Visual Map Route Section
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF1E382B),
                        shadowElevation = 2.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Canvas route lines
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val start = Offset(w * 0.15f, h * 0.75f)
                                val mid = Offset(w * 0.50f, h * 0.45f)
                                val end = Offset(w * 0.82f, h * 0.22f)

                                // Dashed background route
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = start,
                                    end = end,
                                    strokeWidth = 6f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                )

                                // Active completed route
                                drawLine(
                                    color = Color(0xFF38802A),
                                    start = start,
                                    end = mid,
                                    strokeWidth = 6f
                                )
                            }

                            // Dispatched Node (Bottom Left)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = GovindTheme.colors.surfaceContainerLowest
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Warehouse, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(14.dp))
                                    Column {
                                        Text("DISPATCHED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GovindTheme.colors.onSurfaceVariant)
                                        Text("Hub Sec 49", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                                    }
                                }
                            }

                            // Rider Marker (Center)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = (-6).dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(GovindTheme.colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = GovindTheme.colors.surfaceContainerLowest
                                    ) {
                                        Text(
                                            text = "Rajesh • 45 km/h",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GovindTheme.colors.primaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Drop Marker (Top Right)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = GovindTheme.colors.surfaceContainerLowest
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = GovindTheme.colors.tertiaryContainer, modifier = Modifier.size(16.dp))
                                    Column {
                                        Text("DROP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GovindTheme.colors.onSurfaceVariant)
                                        Text("The Crest, Sec 48", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                                    }
                                }
                            }

                            // Signal Badge (Bottom Right)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                shape = CircleShape,
                                color = GovindTheme.colors.surfaceContainerLowest.copy(alpha = 0.9f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.NearMe, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(12.dp))
                                    Text("GPS ±3m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // 3. Delivery Partner Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = GovindTheme.colors.surfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(GovindTheme.colors.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("RK", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Rajesh Kumar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                                        Surface(shape = RoundedCornerShape(4.dp), color = GovindTheme.colors.surfaceContainerHigh) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                                Text("4.9", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text("1,420+ safe Govind deliveries", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                                    Text("Top-Rated Super Captain", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.secondary)
                                }
                            }

                            // Health & Safety Badges
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GovindTheme.colors.surfaceContainerLow)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Thermostat, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(14.dp))
                                    Text("Temp 98.2°F", style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Vaccines, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(14.dp))
                                    Text("Vaccinated", style = MaterialTheme.typography.labelSmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(14.dp))
                                    Text("Sealed Bag", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.surfaceContainerHigh)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Call, contentDescription = null, tint = GovindTheme.colors.primaryContainer, modifier = Modifier.size(16.dp))
                                        Text("Call Rider", color = GovindTheme.colors.primaryContainer, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:+919876543210"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.primaryContainer)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Text("Message", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Live Order Milestones Timeline
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = GovindTheme.colors.surfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Live Order Milestones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                                Text("STEP 4 OF 5", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = GovindTheme.colors.secondary)
                            }

                            TimelineStepItem(
                                title = "Order Placed & Confirmed",
                                subtitle = "Payment verified via StarPay / COD",
                                time = "05:45 PM",
                                isCompleted = true,
                                isActive = false
                            )
                            TimelineStepItem(
                                title = "Farm Pack & Kitchen Cooking",
                                subtitle = "Fresh produce hydro-cooled, Dal Makhani simmering",
                                time = "05:48 PM",
                                isCompleted = true,
                                isActive = false
                            )
                            TimelineStepItem(
                                title = "Quality Inspected & Dispatched",
                                subtitle = "Govind dual-zone cold/hot pouch sealed",
                                time = "05:54 PM",
                                isCompleted = true,
                                isActive = false
                            )
                            TimelineStepItem(
                                title = "Out for Delivery",
                                subtitle = "Rider Rajesh is driving towards Golf Course Ext. Rd",
                                time = "Active Now",
                                isCompleted = false,
                                isActive = true
                            )
                            TimelineStepItem(
                                title = "Arrived at Gate / Delivered",
                                subtitle = "Contactless handoff at Tower reception",
                                time = "Expected 06:16 PM",
                                isCompleted = false,
                                isActive = false
                            )
                        }
                    }
                }

                // 5. Wholesale Separate Shipment Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GovindTheme.colors.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GovindTheme.colors.surfaceContainerHighest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GovindTheme.colors.onSurface, modifier = Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Wholesale Separate Shipment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                                    Surface(shape = CircleShape, color = GovindTheme.colors.surfaceContainerHighest) {
                                        Text("Scheduled", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Your 50kg Nashik Onion Sack is scheduled for tomorrow morning 06:30 AM dock delivery via Govind Freight Truck #HR-55-9012.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GovindTheme.colors.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.clickable { /* track freight */ }
                                ) {
                                    Text("Track Heavy Freight", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.secondary)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // 6. Tax Invoice & WhatsApp Action Buttons
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { /* download invoice */ },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.surfaceContainerHigh)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GovindTheme.colors.onSurface, modifier = Modifier.size(18.dp))
                                Text("Download Detailed Tax Invoice", color = GovindTheme.colors.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919999999999?text=Hi%20Govind%20Support,%20I%20need%20help%20with%20Order%20$orderId"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.surfaceContainerLowest)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(18.dp))
                                Text("Need Help? Chat on WhatsApp", color = GovindTheme.colors.secondary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineStepItem(
    title: String,
    subtitle: String,
    time: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> GovindTheme.colors.secondary
                        isActive -> GovindTheme.colors.primaryContainer
                        else -> GovindTheme.colors.surfaceContainerHigh
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            } else if (isActive) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (isActive) Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GovindTheme.colors.surfaceContainerLow)
                        .padding(8.dp)
                    else Modifier
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) GovindTheme.colors.primaryContainer else GovindTheme.colors.onSurface
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) GovindTheme.colors.secondary else GovindTheme.colors.onSurfaceVariant,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GovindTheme.colors.onSurfaceVariant
            )
        }
    }
}
