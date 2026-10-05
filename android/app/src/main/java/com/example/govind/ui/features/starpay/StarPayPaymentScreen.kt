package com.example.govind.ui.features.starpay

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.govind.data.starpay.StarPayOrderStatus
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarPayPaymentScreen(
    amount: Double,
    orderId: String = "",
    orderRef: String = "",
    description: String = "",
    customerName: String = "",
    customerEmail: String = "",
    customerPhone: String = "",
    internalOrderId: String = "",
    onNavigateBack: () -> Unit,
    onPaymentSuccess: (String, String) -> Unit, // orderId, orderRef
    viewModel: StarPayViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Initialize session once
    LaunchedEffect(Unit) {
        viewModel.initSession(
            amount = amount,
            orderId = orderId,
            orderRef = orderRef,
            description = description,
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            internalOrderId = internalOrderId
        )
    }

    // Lifecycle-aware: Refresh status when user returns from UPI app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto-trigger onPaymentSuccess after 2.5s when PAID
    LaunchedEffect(uiState.status) {
        if (uiState.status == StarPayOrderStatus.PAID) {
            delay(2500)
            onPaymentSuccess(
                uiState.orderId.ifBlank { internalOrderId },
                uiState.orderRef
            )
        }
    }

    // Back handler intercepts to show confirmation
    BackHandler {
        if (uiState.status == StarPayOrderStatus.PAID) {
            onPaymentSuccess(uiState.orderId.ifBlank { internalOrderId }, uiState.orderRef)
        } else {
            viewModel.showCancelConfirmation()
        }
    }

    // Cancel confirmation dialog
    if (uiState.showCancelDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCancelConfirmation() },
            containerColor = StarPayTheme.Surface1,
            titleContentColor = StarPayTheme.TextPrimary,
            textContentColor = StarPayTheme.TextSecondary,
            title = {
                Text(
                    text = "Cancel payment?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "If you have already paid in your UPI app, please wait for auto-verification or submit your 12-digit UTR. Exiting now may delay order confirmation."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissCancelConfirmation()
                        onNavigateBack()
                    }
                ) {
                    Text("Exit Anyway", color = StarPayTheme.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.dismissCancelConfirmation() },
                    colors = ButtonDefaults.buttonColors(containerColor = StarPayTheme.BrandViolet)
                ) {
                    Text("Stay & Pay", color = Color.White)
                }
            }
        )
    }

    // Manual UTR submission Bottom Sheet
    if (uiState.showManualUtrSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeManualUtrSheet() },
            containerColor = StarPayTheme.Surface1,
            contentColor = StarPayTheme.TextPrimary,
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 40.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = StarPayTheme.TextMuted
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Submit Payment Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = StarPayTheme.TextPrimary
                        )
                        Text(
                            text = "Enter 12-digit UTR / UPI Ref from your payment app receipt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StarPayTheme.TextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.closeManualUtrSheet() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StarPayTheme.TextSecondary)
                    }
                }

                // UTR Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "12-DIGIT UTR / TRANSACTION ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = StarPayTheme.BrandVioletLight,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = uiState.utrInput,
                        onValueChange = { viewModel.onUtrChanged(it) },
                        placeholder = { Text("e.g. 427012345678", color = StarPayTheme.TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarPayTheme.BrandViolet,
                            unfocusedBorderColor = StarPayTheme.BorderSubtle,
                            focusedTextColor = StarPayTheme.TextPrimary,
                            unfocusedTextColor = StarPayTheme.TextPrimary,
                            cursorColor = StarPayTheme.BrandViolet
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = StarPayTheme.MonoFont,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        trailingIcon = {
                            Text(
                                text = "${uiState.utrInput.length}/12",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.utrInput.length == 12) StarPayTheme.Emerald else StarPayTheme.TextMuted,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    )
                    if (uiState.utrError != null) {
                        Text(
                            text = uiState.utrError!!,
                            color = StarPayTheme.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Notes Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ADDITIONAL NOTES (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        color = StarPayTheme.TextSecondary
                    )
                    OutlinedTextField(
                        value = uiState.notesInput,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        placeholder = { Text("e.g. Paid via PhonePe / Google Pay", color = StarPayTheme.TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarPayTheme.BrandViolet,
                            unfocusedBorderColor = StarPayTheme.BorderSubtle,
                            focusedTextColor = StarPayTheme.TextPrimary,
                            unfocusedTextColor = StarPayTheme.TextPrimary
                        )
                    )
                }

                // Submit Button
                Button(
                    onClick = { viewModel.submitManualUtr() },
                    enabled = uiState.utrInput.length == 12 && !uiState.isSubmittingUtr,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarPayTheme.BrandViolet,
                        disabledContainerColor = StarPayTheme.Surface2
                    )
                ) {
                    if (uiState.isSubmittingUtr) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = "Submit for Instant Verification",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Main Payment Screen Container
    Scaffold(
        containerColor = StarPayTheme.Surface0,
        topBar = {
            StarPayTopBar(
                onBackClick = {
                    if (uiState.status == StarPayOrderStatus.PAID) {
                        onPaymentSuccess(uiState.orderId.ifBlank { internalOrderId }, uiState.orderRef)
                    } else {
                        viewModel.showCancelConfirmation()
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading Initial Session
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = StarPayTheme.BrandViolet)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Initializing Secure StarPay Session...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarPayTheme.TextSecondary
                        )
                    }
                }

                // STATE E: PAID (Payment Success)
                uiState.status == StarPayOrderStatus.PAID -> {
                    PaidSuccessView(
                        uiState = uiState,
                        onContinue = {
                            onPaymentSuccess(
                                uiState.orderId.ifBlank { internalOrderId },
                                uiState.orderRef
                            )
                        }
                    )
                }

                // STATE F: FAILED (Expired or Rejected)
                uiState.status == StarPayOrderStatus.FAILED -> {
                    FailedExpiredView(
                        uiState = uiState,
                        onRetry = {
                            viewModel.retryPayment(
                                amount = amount,
                                customerName = customerName,
                                customerEmail = customerEmail,
                                customerPhone = customerPhone,
                                internalOrderId = internalOrderId
                            )
                        },
                        onSubmitUtr = { viewModel.openManualUtrSheet() }
                    )
                }

                // Main Flow: AWAITING_PAYMENT, VERIFYING, PENDING_VERIFICATION
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Hero Amount Section
                        HeroAmountCard(
                            uiState = uiState,
                            onCopyAmount = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val amountStr = String.format(Locale.US, "%.2f", uiState.reservedAmount)
                                clipboard.setPrimaryClip(ClipData.newPlainText("StarPay Amount", amountStr))
                                viewModel.setCopiedAmount(true)
                                Toast.makeText(context, "Exact amount ₹$amountStr copied", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 2. State-specific card: VERIFYING or PENDING_VERIFICATION
                        when (uiState.status) {
                            StarPayOrderStatus.VERIFYING -> {
                                VerifyingRadarCard(reservedAmount = uiState.reservedAmount)
                            }
                            StarPayOrderStatus.PENDING_VERIFICATION -> {
                                PendingVerificationCard()
                            }
                            else -> {
                                // 3. State A: Native UPI App Quick-Launch Section
                                UpiQuickLaunchSection(
                                    upiUrl = uiState.upiUrl,
                                    onLaunchUpiApp = { packageName ->
                                        launchUpiIntent(context, uiState.upiUrl, packageName)
                                    }
                                )

                                // 4. Divider OR SCAN QR CODE
                                OrScanQrDivider()

                                // 5. Native QR Code Card
                                QrCodeCard(
                                    uiState = uiState,
                                    onCopyUpiId = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("StarPay UPI ID", uiState.upiId))
                                        viewModel.setCopiedUpiId(true)
                                        Toast.makeText(context, "UPI ID copied: ${uiState.upiId}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // 6. Live Countdown Timer Bar
                        CountdownTimerBar(
                            remainingSeconds = uiState.remainingSeconds
                        )

                        // 7. Manual UTR Trigger Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openManualUtrSheet() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = StarPayTheme.BrandVioletLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Already completed payment? Submit 12-digit UTR manually →",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StarPayTheme.BrandVioletLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// =========================================================================
// SUBCOMPONENTS
// =========================================================================

@Composable
private fun StarPayTopBar(
    onBackClick: () -> Unit
) {
    Surface(
        color = StarPayTheme.Surface0,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = StarPayTheme.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))

                // Lightning badge + Merchant Name
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(StarPayTheme.BrandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "GOVIND",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = StarPayTheme.TextPrimary
                    )
                    Text(
                        text = "Fresh and Healthy Food",
                        style = MaterialTheme.typography.labelSmall,
                        color = StarPayTheme.TextMuted
                    )
                }
            }

            // Secured by StarPay Badge
            Surface(
                shape = CircleShape,
                color = StarPayTheme.Emerald.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.Emerald.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = StarPayTheme.Emerald,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Secured by StarPay",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StarPayTheme.Emerald
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroAmountCard(
    uiState: StarPayUiState,
    onCopyAmount: () -> Unit
) {
    val amountToDisplay = if (uiState.reservedAmount > 0) uiState.reservedAmount else uiState.amount
    val intPart = amountToDisplay.toInt()
    val paisePart = String.format(Locale.US, ".%02d", ((amountToDisplay - intPart) * 100).toInt().coerceAtLeast(0))

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = StarPayTheme.Surface1,
        border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Amount to Pay",
                style = MaterialTheme.typography.labelMedium,
                color = StarPayTheme.TextSecondary
            )

            // Giant split price: White bold integer + Monospace violet paise
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹$intPart",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StarPayTheme.TextPrimary
                )
                Text(
                    text = paisePart,
                    fontSize = 32.sp,
                    fontFamily = StarPayTheme.MonoFont,
                    fontWeight = FontWeight.Bold,
                    color = StarPayTheme.BrandVioletLight,
                    modifier = Modifier.padding(bottom = 3.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // One-tap Copy Amount chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StarPayTheme.Surface2,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle),
                    modifier = Modifier
                        .clickable { onCopyAmount() }
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = StarPayTheme.BrandVioletLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (uiState.isCopiedAmount) "Copied!" else "Copy",
                            style = MaterialTheme.typography.labelSmall,
                            color = StarPayTheme.BrandVioletLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Order description + Monospace Order Reference pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.orderRef.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = StarPayTheme.Surface2
                    ) {
                        Text(
                            text = "Ref: ${uiState.orderRef}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = StarPayTheme.MonoFont,
                            color = StarPayTheme.TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Important Paise Disambiguator Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StarPayTheme.BrandViolet.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BrandViolet.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ Pay exact amount ₹${String.format(Locale.US, "%.2f", amountToDisplay)} (including ${paisePart.replace(".", "")} paise) for instant automatic verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StarPayTheme.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun UpiQuickLaunchSection(
    upiUrl: String,
    onLaunchUpiApp: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "PAY INSTANTLY WITH UPI APP",
            style = MaterialTheme.typography.labelSmall,
            color = StarPayTheme.TextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        val upiApps = listOf(
            Triple("GPay", "com.google.android.apps.nbu.paisa.user", Color(0xFF4285F4)),
            Triple("PhonePe", "com.phonepe.app", Color(0xFF5F259F)),
            Triple("Paytm", "net.one97.paytm", Color(0xFF00B9F1)),
            Triple("BHIM", "in.org.npci.upiapp", Color(0xFFF57C00))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            upiApps.forEach { (name, pkg, accent) ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StarPayTheme.Surface2,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp)
                        .clickable { onLaunchUpiApp(pkg) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1),
                                color = accent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StarPayTheme.TextPrimary
                        )
                    }
                }
            }
        }

        // Full-width primary gradient button: "Open Any UPI App →"
        Button(
            onClick = { onLaunchUpiApp("") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StarPayTheme.BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Open Any UPI App",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrScanQrDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = StarPayTheme.BorderSubtle)
        Text(
            text = "OR SCAN QR CODE",
            style = MaterialTheme.typography.labelSmall,
            color = StarPayTheme.TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = StarPayTheme.BorderSubtle)
    }
}

@Composable
private fun QrCodeCard(
    uiState: StarPayUiState,
    onCopyUpiId: () -> Unit
) {
    val bitmap = remember(uiState.qrBase64) {
        if (uiState.qrBase64.isNotBlank()) {
            try {
                val bytes = Base64.decode(uiState.qrBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = StarPayTheme.Surface1,
        border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // White QR Container (200dp x 200dp)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "StarPay QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(
                            color = StarPayTheme.BrandViolet,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Text(
                text = "Scan with any UPI app",
                style = MaterialTheme.typography.bodySmall,
                color = StarPayTheme.TextSecondary
            )

            // Monospace UPI ID Row with Copy button
            if (uiState.upiId.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StarPayTheme.Surface2,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.upiId,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = StarPayTheme.MonoFont,
                            color = StarPayTheme.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = onCopyUpiId,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy UPI ID",
                                tint = StarPayTheme.BrandVioletLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownTimerBar(
    remainingSeconds: Long
) {
    val minutes = (remainingSeconds / 60).coerceAtLeast(0)
    val seconds = (remainingSeconds % 60).coerceAtLeast(0)
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val tintColor = when {
        remainingSeconds > 120 -> StarPayTheme.Emerald
        remainingSeconds in 60..120 -> StarPayTheme.Amber
        else -> StarPayTheme.Red
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tintColor.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tintColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "QR & session expires in",
                    style = MaterialTheme.typography.bodySmall,
                    color = StarPayTheme.TextSecondary
                )
            }

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = StarPayTheme.MonoFont,
                fontWeight = FontWeight.Bold,
                color = tintColor
            )
        }
    }
}

@Composable
private fun VerifyingRadarCard(
    reservedAmount: Double
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = StarPayTheme.Surface1,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, StarPayTheme.BrandViolet),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((72 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(StarPayTheme.BrandViolet.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = StarPayTheme.BrandViolet,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
            }

            Text(
                text = "Payment Detected — Verifying...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StarPayTheme.TextPrimary
            )
            Text(
                text = "Matching amount and UTR with bank records. Please do not close this screen.",
                style = MaterialTheme.typography.bodySmall,
                color = StarPayTheme.TextSecondary,
                textAlign = TextAlign.Center
            )

            // Step Checklist
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StarPayTheme.Surface2, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StarPayTheme.Emerald, modifier = Modifier.size(16.dp))
                    Text("SMS / UPI Notification Received", style = MaterialTheme.typography.bodySmall, color = StarPayTheme.TextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StarPayTheme.Emerald, modifier = Modifier.size(16.dp))
                    Text("Matching Exact Amount (₹${String.format(Locale.US, "%.2f", reservedAmount)})", style = MaterialTheme.typography.bodySmall, color = StarPayTheme.TextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = StarPayTheme.BrandViolet, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Text("Verifying UTR Uniqueness & Security", style = MaterialTheme.typography.bodySmall, color = StarPayTheme.BrandVioletLight)
                }
            }
        }
    }
}

@Composable
private fun PendingVerificationCard() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = StarPayTheme.Surface1,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, StarPayTheme.Amber),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(StarPayTheme.Amber.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = StarPayTheme.Amber,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "Payment Under Review",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StarPayTheme.TextPrimary
            )

            Text(
                text = "Your payment details (UTR) have been submitted. Our team is verifying your transaction — this screen will update automatically once approved.",
                style = MaterialTheme.typography.bodySmall,
                color = StarPayTheme.TextSecondary,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularProgressIndicator(
                    color = StarPayTheme.Amber,
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Awaiting verification approval...",
                    style = MaterialTheme.typography.labelSmall,
                    color = StarPayTheme.Amber
                )
            }
        }
    }
}

@Composable
private fun PaidSuccessView(
    uiState: StarPayUiState,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing emerald checkmark circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(StarPayTheme.Emerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = StarPayTheme.Emerald,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Payment Successful!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = StarPayTheme.TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your payment of ₹${String.format(Locale.US, "%.2f", uiState.reservedAmount.coerceAtLeast(uiState.amount))} has been verified and confirmed.",
            style = MaterialTheme.typography.bodyMedium,
            color = StarPayTheme.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Receipt Summary Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StarPayTheme.Surface2,
            border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReceiptRow("Order Reference", uiState.orderRef, isMono = true)
                ReceiptRow("Amount Paid", "₹${String.format(Locale.US, "%.2f", uiState.reservedAmount.coerceAtLeast(uiState.amount))}", valueColor = StarPayTheme.Emerald)
                if (uiState.upiTxnRef.isNotBlank()) {
                    ReceiptRow("UPI Ref", uiState.upiTxnRef, isMono = true)
                }
                ReceiptRow("Status", "VERIFIED ✓", valueColor = StarPayTheme.Emerald)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StarPayTheme.Emerald)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF020617)
            )
        }
    }
}

@Composable
private fun FailedExpiredView(
    uiState: StarPayUiState,
    onRetry: () -> Unit,
    onSubmitUtr: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(StarPayTheme.Red.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Expired",
                tint = StarPayTheme.Red,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Payment Session Expired",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = StarPayTheme.TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "This payment request has expired or failed. If money was deducted, please submit your UTR or contact support.",
            style = MaterialTheme.typography.bodyMedium,
            color = StarPayTheme.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StarPayTheme.BrandViolet)
        ) {
            Text("Retry / Create New Payment", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onSubmitUtr,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StarPayTheme.BorderSubtle)
        ) {
            Text("Money Deducted? Submit UTR", color = StarPayTheme.BrandVioletLight)
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    valueColor: Color = StarPayTheme.TextPrimary,
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = StarPayTheme.TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = if (isMono) StarPayTheme.MonoFont else null,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

// UPI Intent launcher helper
private fun launchUpiIntent(context: Context, upiUrl: String, targetPackage: String = "") {
    if (upiUrl.isBlank()) {
        Toast.makeText(context, "UPI payment link not ready yet", Toast.LENGTH_SHORT).show()
        return
    }

    val uri = Uri.parse(upiUrl)
    val intent = Intent(Intent.ACTION_VIEW, uri)

    if (targetPackage.isNotBlank()) {
        intent.setPackage(targetPackage)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Gracefully fall back to generic chooser
            Toast.makeText(context, "App not installed, showing available UPI apps", Toast.LENGTH_SHORT).show()
            val chooser = Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), "Pay with UPI")
            try {
                context.startActivity(chooser)
            } catch (ex: Exception) {
                Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_LONG).show()
            }
        }
    } else {
        val chooser = Intent.createChooser(intent, "Pay with UPI")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_LONG).show()
        }
    }
}
