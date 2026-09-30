package com.example.govind.ui.features.delivery

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.DeliveryTrackingService
import com.example.govind.data.model.Order
import com.example.govind.theme.GovindTheme
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryPartnerScreen(
    viewModel: DeliveryPartnerViewModel = hiltViewModel(),
    onLogoutSuccess: () -> Unit = {}
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingOrderIdForTracking by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineLocationGranted || coarseLocationGranted) {
            pendingOrderIdForTracking?.let { orderId ->
                viewModel.startDelivery(orderId) {
                    val intent = Intent(context, DeliveryTrackingService::class.java).apply {
                        action = DeliveryTrackingService.ACTION_START
                        putExtra(DeliveryTrackingService.EXTRA_ORDER_ID, orderId)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    Toast.makeText(context, "Delivery started. GPS broadcasting active.", Toast.LENGTH_SHORT).show()
                }
                pendingOrderIdForTracking = null
            }
        } else {
            Toast.makeText(context, "Location permission is required for live delivery tracking.", Toast.LENGTH_LONG).show()
            pendingOrderIdForTracking = null
        }
    }

    fun requestStartDelivery(orderId: String) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.startDelivery(orderId) {
                val intent = Intent(context, DeliveryTrackingService::class.java).apply {
                    action = DeliveryTrackingService.ACTION_START
                    putExtra(DeliveryTrackingService.EXTRA_ORDER_ID, orderId)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Toast.makeText(context, "Delivery started. GPS broadcasting active.", Toast.LENGTH_SHORT).show()
            }
        } else {
            pendingOrderIdForTracking = orderId
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Delivery Partner Portal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.textPrimary
                        )
                        val driverName = profile?.fullName ?: profile?.name ?: "Delivery Partner"
                        Text(
                            text = "Logged in: $driverName",
                            style = MaterialTheme.typography.bodySmall,
                            color = GovindTheme.colors.textSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchOrders() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Deliveries",
                            tint = GovindTheme.colors.govindGreen
                        )
                    }
                    IconButton(onClick = {
                        viewModel.logout {
                            val stopIntent = Intent(context, DeliveryTrackingService::class.java).apply {
                                action = DeliveryTrackingService.ACTION_STOP
                            }
                            context.startService(stopIntent)
                            onLogoutSuccess()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFFC62828)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GovindTheme.colors.warmWhite
                )
            )
        },
        containerColor = GovindTheme.colors.warmWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Summary Banner
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Deliveries",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "${orders.size} order(s) assigned to you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Surface(
                        color = GovindTheme.colors.govindGreen,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${orders.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading && orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GovindTheme.colors.govindGreen)
                }
            } else if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GovindTheme.colors.govindGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Pending Deliveries",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You're all caught up! New orders will appear here once assigned.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GovindTheme.colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        DeliveryOrderCardItem(
                            order = order,
                            onStartDelivery = { requestStartDelivery(order.id) },
                            onMarkDelivered = {
                                viewModel.markDelivered(order.id) {
                                    val intent = Intent(context, DeliveryTrackingService::class.java).apply {
                                        action = DeliveryTrackingService.ACTION_STOP
                                    }
                                    context.startService(intent)
                                    Toast.makeText(context, "Order marked delivered! Great job.", Toast.LENGTH_SHORT).show()
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
fun DeliveryOrderCardItem(
    order: Order,
    onStartDelivery: () -> Unit,
    onMarkDelivered: () -> Unit
) {
    val context = LocalContext.current
    val isOutForDelivery = order.orderStatus.uppercase() == "OUT_FOR_DELIVERY"

    // Extract address fields safely from addressSnapshot or addresses
    val snapshot = order.addressSnapshot
    val customerName = snapshot?.get("name")?.jsonPrimitive?.content
        ?: order.addresses?.name
        ?: "Customer"

    val customerPhone = snapshot?.get("phone")?.jsonPrimitive?.content
        ?: order.addresses?.phone

    val house = snapshot?.get("house")?.jsonPrimitive?.content ?: order.addresses?.house ?: ""
    val street = snapshot?.get("street")?.jsonPrimitive?.content ?: order.addresses?.street ?: ""
    val area = snapshot?.get("area")?.jsonPrimitive?.content ?: order.addresses?.area ?: ""
    val city = snapshot?.get("city")?.jsonPrimitive?.content ?: order.addresses?.city ?: ""
    val pincode = snapshot?.get("pincode")?.jsonPrimitive?.content ?: order.addresses?.pincode ?: ""

    val fullAddress = listOf(house, street, area, city, pincode).filter { it.isNotBlank() }.joinToString(", ")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GovindTheme.colors.pureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Order ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.textPrimary
                    )
                    Text(
                        text = "${order.paymentMethod} • ₹${String.format("%.0f", order.total)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GovindTheme.colors.govindGreen
                    )
                }

                Surface(
                    color = if (isOutForDelivery) Color(0xFFE0F7FA) else Color(0xFFEDE7F6),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isOutForDelivery) "OUT FOR DELIVERY" else "READY FOR PICKUP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOutForDelivery) Color(0xFF00695C) else Color(0xFF512DA8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Destination & Customer Info
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = GovindTheme.colors.govindGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.textPrimary
                    )
                    if (fullAddress.isNotBlank()) {
                        Text(
                            text = fullAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = GovindTheme.colors.textSecondary
                        )
                    }
                }

                if (!customerPhone.isNullOrBlank()) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$customerPhone")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Customer",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (isOutForDelivery) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFE0F2F1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00796B), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Live GPS Broadcast Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isOutForDelivery) {
                    Button(
                        onClick = onStartDelivery,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Delivery (Enable GPS)")
                    }
                } else {
                    Button(
                        onClick = onMarkDelivered,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Order Delivered")
                    }
                }
            }
        }
    }
}
