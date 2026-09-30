package com.example.govind.ui.features.support

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.govind.theme.GovindTheme

data class FaqItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val faqs = remember {
        listOf(
            FaqItem(
                question = "What are Govind's delivery hours?",
                answer = "Govind delivers fresh farm produce and Punjabi Kitchen orders between 6:00 AM and 10:00 PM daily across Gurugram. Wholesale mandi sacks are dispatched in early morning slots by 7:00 AM."
            ),
            FaqItem(
                question = "How does the Unified Global Cart work?",
                answer = "You can seamlessly add Fresh farm vegetables, Kitchen hot thalis, and Wholesale bulk bags into ONE shopping basket. Fresh and Kitchen items are prepared and dispatched together, while wholesale bulk orders follow their scheduled mandi truck slots."
            ),
            FaqItem(
                question = "What is your refund policy on fresh produce and meals?",
                answer = "Govind offers a 100% Quality Guarantee. If any greens, milk, paneer, or hot food item is damaged or unsatisfactory, report it within 2 hours of delivery for an instant, no-questions-asked refund or free replacement."
            ),
            FaqItem(
                question = "What payment options are available?",
                answer = "We support Cash on Delivery (COD) across all pincodes, as well as UPI (Google Pay, PhonePe, Paytm), Credit/Debit Cards, and NetBanking via our checkout system."
            ),
            FaqItem(
                question = "How do I track my active delivery?",
                answer = "When your order status becomes OUT_FOR_DELIVERY, navigate to 'My Orders' -> tap your order to view live GPS tracking with real-time ETA in minutes, straight-line distance, and a direct call button for your delivery partner."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Govind Care & Help",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Support Hero Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GovindTheme.colors.primary,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "How can we help you?",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Our customer support team is available 7 days a week to ensure your farm fresh groceries and kitchen meals arrive on time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = GovindTheme.colors.secondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Operational 6:00 AM - 10:00 PM IST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = GovindTheme.colors.secondaryContainer
                            )
                        }
                    }
                }
            }

            // 2. Direct Contact Buttons
            item {
                Text(
                    text = "DIRECT CONTACT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.onSurfaceVariant
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Call Button
                    ContactActionCard(
                        title = "Call Us",
                        subtitle = "+91 98100 00000",
                        icon = Icons.Default.Call,
                        tint = GovindTheme.colors.govindGreen,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919810000000"))
                            context.startActivity(intent)
                        }
                    )

                    // WhatsApp Button
                    ContactActionCard(
                        title = "WhatsApp",
                        subtitle = "Instant Chat",
                        icon = Icons.Default.Chat,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919810000000?text=Hello%20Govind%20Care"))
                            context.startActivity(intent)
                        }
                    )

                    // Email Button
                    ContactActionCard(
                        title = "Email",
                        subtitle = "care@govind.farm",
                        icon = Icons.Default.Email,
                        tint = GovindTheme.colors.govindOrange,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:care@govind.farm?subject=Govind%20Support%20Inquiry"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // 3. Frequently Asked Questions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FREQUENTLY ASKED QUESTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.onSurfaceVariant
                )
            }

            items(faqs) { faq ->
                FaqAccordionCard(faq = faq)
            }
        }
    }
}

@Composable
fun ContactActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GovindTheme.colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GovindTheme.colors.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = GovindTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FaqAccordionCard(faq: FaqItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GovindTheme.colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GovindTheme.colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = GovindTheme.colors.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GovindTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
