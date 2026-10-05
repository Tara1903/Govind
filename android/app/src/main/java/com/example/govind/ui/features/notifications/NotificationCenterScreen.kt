package com.example.govind.ui.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.govind.data.repository.NotificationItem
import com.example.govind.theme.GovindTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNotifications() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Notifications", fontWeight = FontWeight.Bold)
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .background(GovindTheme.colors.govindGreen, CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("$unreadCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllRead() }) {
                            Text("Mark All Read", color = GovindTheme.colors.govindGreen, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GovindTheme.colors.warmWhite)
            )
        },
        containerColor = GovindTheme.colors.warmWhite
    ) { padding ->
        if (isLoading && notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GovindTheme.colors.govindGreen)
            }
        } else if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Notifications, contentDescription = null,
                        modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("No notifications yet", color = Color.Gray, fontSize = 16.sp)
                    Text("Order updates and offers will appear here", color = Color.LightGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Group by date
                val today = LocalDate.now(ZoneId.of("Asia/Kolkata"))
                val grouped = notifications.groupBy { item ->
                    try {
                        val date = java.time.Instant.parse(item.createdAt)
                            .atZone(ZoneId.of("Asia/Kolkata")).toLocalDate()
                        when {
                            date == today -> "Today"
                            date == today.minusDays(1) -> "Yesterday"
                            else -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        }
                    } catch (e: Exception) { "Earlier" }
                }

                grouped.forEach { (dateLabel, items) ->
                    item {
                        Text(
                            dateLabel,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                    items(items) { notif ->
                        NotificationItemRow(
                            notification = notif,
                            onClick = {
                                viewModel.markRead(notif.id)
                                notif.deepLink?.let { link ->
                                    val route = when {
                                        link.startsWith("govind://order/") -> {
                                            val id = link.substringAfter("govind://order/").substringBefore("/")
                                            "order_details/$id"
                                        }
                                        link == "govind://cart" -> "cart"
                                        link.startsWith("govind://product/") -> {
                                            val id = link.substringAfter("govind://product/")
                                            "product/$id"
                                        }
                                        else -> null
                                    }
                                    route?.let(onNavigate)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemRow(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val govindGreen = GovindTheme.colors.govindGreen

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (!notification.isRead) govindGreen.copy(alpha = 0.04f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    when (notification.type) {
                        "ORDER", "DELIVERY" -> govindGreen.copy(alpha = 0.1f)
                        "CART" -> Color(0xFFF5450D).copy(alpha = 0.1f)
                        "OFFER", "MARKETING" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else -> Color.LightGray.copy(alpha = 0.2f)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (notification.type) {
                    "ORDER" -> Icons.Filled.ShoppingBag
                    "DELIVERY" -> Icons.Filled.LocalShipping
                    "CART" -> Icons.Filled.ShoppingCart
                    "OFFER", "MARKETING" -> Icons.Filled.LocalOffer
                    "FAVORITE" -> Icons.Filled.Favorite
                    "ACCOUNT" -> Icons.Filled.Person
                    else -> Icons.Filled.Notifications
                },
                contentDescription = null,
                tint = when (notification.type) {
                    "ORDER", "DELIVERY" -> govindGreen
                    "CART" -> Color(0xFFF5450D)
                    else -> govindGreen
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(govindGreen, CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                notification.body,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatTime(notification.createdAt),
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
}

private fun formatTime(isoTime: String): String {
    return try {
        val instant = java.time.Instant.parse(isoTime)
        val zonedTime = instant.atZone(ZoneId.of("Asia/Kolkata"))
        DateTimeFormatter.ofPattern("hh:mm a").format(zonedTime)
    } catch (e: Exception) { "" }
}
