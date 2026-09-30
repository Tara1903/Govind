package com.example.govind.ui.features.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.govind.theme.GovindTheme

enum class LegalTab(val title: String) {
    PRIVACY("Privacy Policy"),
    TERMS("Terms of Service"),
    REFUND("Refund Policy"),
    CANCELLATION("Cancellation")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    initialTab: String = "privacy",
    onNavigateBack: () -> Unit
) {
    val initialEnum = when (initialTab.lowercase()) {
        "terms" -> LegalTab.TERMS
        "refund" -> LegalTab.REFUND
        "cancellation" -> LegalTab.CANCELLATION
        else -> LegalTab.PRIVACY
    }

    var selectedTab by remember { mutableStateOf(initialEnum) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Legal & Compliance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GovindTheme.colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GovindTheme.colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GovindTheme.colors.surfaceContainerLowest
                )
            )
        },
        containerColor = GovindTheme.colors.surfaceContainerLow
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = GovindTheme.colors.surfaceContainerLowest,
                contentColor = GovindTheme.colors.govindGreen,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = GovindTheme.colors.govindGreen
                    )
                }
            ) {
                LegalTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) GovindTheme.colors.govindGreen else GovindTheme.colors.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GovindTheme.colors.surfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        LegalTab.PRIVACY -> PrivacyPolicyContent()
                        LegalTab.TERMS -> TermsContent()
                        LegalTab.REFUND -> RefundContent()
                        LegalTab.CANCELLATION -> CancellationContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Text("Govind Privacy Policy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
    Text("Last Updated: September 2026", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant)
    
    Text(
        "1. Information We Collect\n" +
        "Govind Farm Enterprises (\"Govind\", \"we\", \"our\") collects user information to deliver high-quality farm produce, authentic kitchen meals, and wholesale mandi orders. This includes: contact details (email address, mobile phone number), delivery addresses, and order history.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "2. Geolocation Data\n" +
        "When an order is in transit, approximate location data is collected from designated Delivery Partners to provide real-time tracking, ETA calculations, and route efficiency. Customer locations are used solely for order drop-off verification.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "3. Security & Retention\n" +
        "Your data is protected under strict Row Level Security (RLS) policies within our database architecture. We do not sell, rent, or trade your personal information with external advertisers.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "4. Contact Privacy Officer\n" +
        "For inquiries regarding your data rights or account deletion requests, email privacy@govind.farm.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )
}

@Composable
private fun TermsContent() {
    Text("Terms of Service", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
    Text("Effective Date: September 2026", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant)

    Text(
        "1. Acceptance of Terms\n" +
        "By accessing or using the Govind Android application, web portal, or ordering services, you agree to be bound by these Terms of Service and applicable Indian laws including the Information Technology Act, 2000.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "2. Commercial Offerings & Multi-Experience Structure\n" +
        "Govind operates three specialized experiences: Fresh (farm vegetables & dairy), Kitchen (prepared hot meals & parathas), and Wholesale (B2B mandi sack rates). Pricing, minimum order values, and dispatch timelines vary by experience and are calculated transparently by our PricingEngine.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "3. Pricing & Mandi Market Volatility\n" +
        "Prices for fresh agricultural produce reflect daily mandi market valuations and may adjust each morning. Once an order is placed and confirmed, the price snapshot recorded at checkout is guaranteed and non-adjustable.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "4. Delivery & Drop-off\n" +
        "Delivery commitments are subject to road conditions, weather, and accurate recipient contact information. Failure to receive delivery after 3 partner attempts may result in order forfeiture.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )
}

@Composable
private fun RefundContent() {
    Text("Refund & Return Policy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
    Text("Compliance: Consumer Protection (E-Commerce) Rules, 2020", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant)

    Text(
        "1. Perishable Quality Guarantee\n" +
        "Due to the perishable nature of fresh green vegetables, herbs, dairy, and freshly prepared hot Punjabi meals, conventional multi-day returns are not applicable. Instead, Govind provides an instant quality resolution protocol.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "2. 2-Hour Reporting Window\n" +
        "If any item delivered is spoiled, physically damaged, or missing, notify Govind Care via in-app chat or phone within 2 hours of delivery. A photograph of the affected item may be requested.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "3. Refund Mechanism\n" +
        "For Cash on Delivery orders, refunds are issued via instant UPI transfer or stored credit. For online payments, refunds are processed back to the original source instrument within 3-5 business days.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )
}

@Composable
private fun CancellationContent() {
    Text("Order Cancellation Policy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = GovindTheme.colors.onSurface)
    Text("Notice for Customer Orders", style = MaterialTheme.typography.labelSmall, color = GovindTheme.colors.onSurfaceVariant)

    Text(
        "1. Cancellation Before Preparation\n" +
        "Customers may cancel an order free of charge while its status remains in 'PLACED' or 'PENDING'. Once the kitchen or farm packhouse begins preparation ('PREPARING' status), orders cannot be cancelled due to custom ingredient allocation.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "2. Wholesale Order Cancellations\n" +
        "Wholesale bulk orders (e.g., 50kg onion bags or 100kg potato sacks) require mandi dispatch planning and can only be cancelled before 6:00 AM on the day of delivery.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )

    Text(
        "3. Govind Right of Cancellation\n" +
        "Govind reserves the right to cancel any order due to stock depletion, extreme weather disruptions, or unserviceable address sectors. Full refunds will be disbursed immediately in such events.",
        style = MaterialTheme.typography.bodyMedium,
        color = GovindTheme.colors.onSurface
    )
}
