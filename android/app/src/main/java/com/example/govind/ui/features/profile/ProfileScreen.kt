package com.example.govind.ui.features.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.theme.GovindTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToSavedAddresses: () -> Unit = {},
    onNavigateToAddAddress: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToLegal: (String) -> Unit = {},
    onNavigateToDeliveryPartner: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPhoneDialog by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("") }
    var whatsappNotifications by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("ENG") }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onNavigateToAuth()
        }
    }

    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text("Delivery Contact Phone", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter your 10-digit mobile number for delivery partner contact and order updates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { if (it.length <= 10) phoneInput = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("9876543210") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePhone(phoneInput)
                        showPhoneDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.secondary)
                ) {
                    Text("Save Phone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false }) {
                    Text("Cancel", color = GovindTheme.colors.onSurfaceVariant)
                }
            }
        )
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GovindTheme.colors.onSurface
                            )
                        }
                        Text(
                            text = "Account Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GovindTheme.colors.onSurface
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = GovindTheme.colors.tertiaryFixed
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Bolt, contentDescription = null, tint = GovindTheme.colors.tertiaryContainer, modifier = Modifier.size(14.dp))
                            Text("12 Mins", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onTertiaryFixed)
                        }
                    }
                }
            }
        },
        containerColor = GovindTheme.colors.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Hero Card with Expressive VIP Gradient
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        GovindTheme.colors.primaryContainer,
                                        Color(0xFF002D11),
                                        GovindTheme.colors.primaryContainer
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Top Profile Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(GovindTheme.colors.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.initial,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GovindTheme.colors.onSecondaryContainer
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = uiState.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = GovindTheme.colors.secondaryContainer,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    phoneInput = uiState.phone
                                                    showPhoneDialog = true
                                                }
                                        )
                                    }
                                    val contactText = if (uiState.phone.isNotBlank()) "${uiState.phone} • ${uiState.email}" else uiState.email.ifBlank { "Contact phone not added" }
                                    Text(
                                        text = contactText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GovindTheme.colors.surfaceVariant
                                    )
                                }
                            }

                            // VIP Membership Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(GovindTheme.colors.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Stars, contentDescription = null, tint = GovindTheme.colors.onSecondaryContainer, modifier = Modifier.size(16.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("GOVIND Club VIP Member", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.secondaryContainer)
                                            Text("Active", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                                        }
                                        Text("₹0 Delivery Fee on all Fresh & Kitchen orders", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                                    }
                                }
                            }

                            // Wallet & Credit Balance Bento Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.12f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("StarPay Balance", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.surfaceVariant)
                                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GovindTheme.colors.secondaryContainer, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Text("₹1,250", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                            Text("Top Up", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.secondaryContainer, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.12f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Vyapar Credit", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.surfaceVariant)
                                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = GovindTheme.colors.secondaryContainer, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Text("₹25,000", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                            Text("Available", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.secondaryContainer)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Active Ongoing Order Pill Tracker
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GovindTheme.colors.surfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToOrders() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GovindTheme.colors.secondaryContainer.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("1 Order In Transit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GovindTheme.colors.secondary))
                                }
                                Text("Arriving in approx 14 mins (Fresh + Kitchen)", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Track", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.secondary)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 3. Quick Switcher Experience Horizon
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Switch Catalog Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                        Text("Tap to Explore", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExperienceHorizonCard(
                            title = "Fresh",
                            badge = "12m",
                            badgeColor = GovindTheme.colors.secondaryContainer,
                            badgeTextColor = GovindTheme.colors.onSecondaryContainer,
                            subtitle = "Farm veg & dairy",
                            icon = Icons.Default.Eco,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                com.example.govind.ui.navigation.AppState.switchExperience("FRESH")
                                onNavigateBack()
                            }
                        )
                        ExperienceHorizonCard(
                            title = "Kitchen",
                            badge = "Hot",
                            badgeColor = GovindTheme.colors.tertiaryFixed,
                            badgeTextColor = GovindTheme.colors.onTertiaryFixed,
                            subtitle = "Punjabi thalis",
                            icon = Icons.Default.Restaurant,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                com.example.govind.ui.navigation.AppState.switchExperience("KITCHEN")
                                onNavigateBack()
                            }
                        )
                        ExperienceHorizonCard(
                            title = "Wholesale",
                            badge = "B2B",
                            badgeColor = GovindTheme.colors.surfaceContainerHigh,
                            badgeTextColor = GovindTheme.colors.onSurface,
                            subtitle = "Mandi bulk rates",
                            icon = Icons.Default.Inventory2,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                com.example.govind.ui.navigation.AppState.switchExperience("WHOLESALE")
                                onNavigateBack()
                            }
                        )
                    }
                }
            }

            // 4. Section 1: Shopping & Orders
            item {
                val shoppingItems = buildList {
                    add(
                        ProfileRowItem(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "My Orders",
                            subtitle = "View order history and live deliveries",
                            onClick = onNavigateToOrders
                        )
                    )
                    add(
                        ProfileRowItem(
                            icon = Icons.Default.Favorite,
                            title = "Favorites & Frequent Reorders",
                            subtitle = "View and reorder saved favorites",
                            onClick = onNavigateToFavorites
                        )
                    )
                    add(
                        ProfileRowItem(
                            icon = Icons.Default.Analytics,
                            title = "Today's Mandi Rate List",
                            subtitle = "Daily Gurugram wholesale market prices",
                            badge = "PDF",
                            onClick = {
                                com.example.govind.ui.navigation.AppState.switchExperience("WHOLESALE")
                                onNavigateBack()
                            }
                        )
                    )
                    if (uiState.role.equals("delivery", ignoreCase = true) ||
                        uiState.role.equals("DELIVERY_PARTNER", ignoreCase = true) ||
                        uiState.role.equals("ADMIN", ignoreCase = true)
                    ) {
                        add(
                            ProfileRowItem(
                                icon = Icons.Default.LocalShipping,
                                title = "Delivery Partner Dashboard",
                                subtitle = "Active delivery assignments and live tracking",
                                badge = "Partner",
                                onClick = onNavigateToDeliveryPartner
                            )
                        )
                    }
                }
                ProfileSectionGroup(
                    title = "Shopping & Orders",
                    items = shoppingItems
                )
            }

            // 5. Section 2: Addresses & Payment
            item {
                ProfileSectionGroup(
                    title = "Addresses & Payment",
                    items = listOf(
                        ProfileRowItem(
                            icon = Icons.Default.LocationOn,
                            title = "Saved Addresses",
                            subtitle = "Manage home, work, and warehouse locations",
                            onClick = onNavigateToSavedAddresses
                        ),
                        ProfileRowItem(
                            icon = Icons.Default.CreditCard,
                            title = "StarPay & Saved Cards",
                            subtitle = "UPI VPAs, StarPay 1-Click activated",
                            onClick = {}
                        ),
                        ProfileRowItem(
                            icon = Icons.Default.Domain,
                            title = "GST & Business Invoicing",
                            subtitle = "Govind Vyapar B2B Tax invoice profile",
                            onClick = {}
                        )
                    )
                )
            }

            // 6. Section 3: Offers & Loyalty Bento
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = GovindTheme.colors.surfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(GovindTheme.colors.tertiaryFixed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = GovindTheme.colors.tertiaryContainer, modifier = Modifier.size(16.dp))
                                }
                                Surface(shape = CircleShape, color = GovindTheme.colors.tertiaryContainer) {
                                    Text("3 ACTIVE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Column {
                                Text("Coupons", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Save up to 40%", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = GovindTheme.colors.surfaceContainerLowest,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(GovindTheme.colors.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Savings, contentDescription = null, tint = GovindTheme.colors.secondary, modifier = Modifier.size(16.dp))
                                }
                                Text("LIFETIME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.secondary)
                            }
                            Column {
                                Text("₹14,820", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = GovindTheme.colors.secondary)
                                Text("Saved with GOVIND", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 7. Section 4: Preferences & Support
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GovindTheme.colors.surfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Dietary Notes
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = GovindTheme.colors.primaryContainer)
                                Column {
                                    Text("Dietary & Cooking Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Jain, Less Spicy, Desi Ghee", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GovindTheme.colors.onSurfaceVariant)
                        }

                        HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh)

                        // Notifications Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GovindTheme.colors.primaryContainer)
                                Column {
                                    Text("WhatsApp & SMS Updates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Order alerts & live rider tracking", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = whatsappNotifications,
                                onCheckedChange = { whatsappNotifications = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GovindTheme.colors.secondary
                                )
                            )
                        }

                        HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh)

                        // 24/7 Support
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSupport() }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = GovindTheme.colors.primaryContainer)
                                Column {
                                    Text("Help & 24/7 Support Desk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Chat on WhatsApp, Call, or Email Support", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GovindTheme.colors.secondary)
                        }

                        HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh)

                        // Legal Policies & Compliance
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToLegal("privacy") }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = GovindTheme.colors.primaryContainer)
                                Column {
                                    Text("Legal & Policies", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Privacy, Terms, Refund & Cancellation Policies", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GovindTheme.colors.secondary)
                        }

                        HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh)

                        // App Language Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = GovindTheme.colors.primaryContainer)
                                Column {
                                    Text("App Language", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("English (Default)", style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GovindTheme.colors.surfaceContainer
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (selectedLanguage == "ENG") GovindTheme.colors.surfaceContainerLowest else Color.Transparent,
                                        modifier = Modifier.clickable { selectedLanguage = "ENG" }
                                    ) {
                                        Text("ENG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (selectedLanguage == "HIN") GovindTheme.colors.surfaceContainerLowest else Color.Transparent,
                                        modifier = Modifier.clickable { selectedLanguage = "HIN" }
                                    ) {
                                        Text("हिंदी", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Log Out Action & Version Info
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GovindTheme.colors.error.copy(alpha = 0.1f),
                            contentColor = GovindTheme.colors.error
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = GovindTheme.colors.error, modifier = Modifier.size(18.dp))
                            Text("Log Out from Account", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GOVIND MULTI-COMMERCE v4.2.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = GovindTheme.colors.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Build 2026 • Made with ❤️ for NCR",
                        style = MaterialTheme.typography.bodySmall,
                        color = GovindTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ExperienceHorizonCard(
    title: String,
    badge: String,
    badgeColor: Color,
    badgeTextColor: Color,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = GovindTheme.colors.surfaceContainerLow,
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(GovindTheme.colors.surfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = GovindTheme.colors.primaryContainer, modifier = Modifier.size(18.dp))
                }
                Surface(shape = RoundedCornerShape(4.dp), color = badgeColor) {
                    Text(badge, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold, color = badgeTextColor, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                }
            }
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = GovindTheme.colors.onSurfaceVariant, maxLines = 1)
        }
    }
}

data class ProfileRowItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val badge: String? = null,
    val onClick: () -> Unit
)

@Composable
fun ProfileSectionGroup(title: String, items: List<ProfileRowItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GovindTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GovindTheme.colors.surfaceContainerLowest,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { item.onClick() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(GovindTheme.colors.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(item.icon, contentDescription = null, tint = GovindTheme.colors.primaryContainer, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
                                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = GovindTheme.colors.onSurfaceVariant)
                            }
                        }

                        if (item.badge != null) {
                            Surface(shape = CircleShape, color = GovindTheme.colors.secondaryContainer) {
                                Text(item.badge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                        } else {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GovindTheme.colors.onSurfaceVariant)
                        }
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(color = GovindTheme.colors.surfaceContainerHigh, modifier = Modifier.padding(start = 54.dp))
                    }
                }
            }
        }
    }
}
