package com.example.govind.ui.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.govind.data.model.Product
import com.example.govind.theme.Dimens
import com.example.govind.theme.GovindTheme

// ═════════════════════════════════════════════════════════════
// 1. FSSAI VEG / NON-VEG INDICATOR
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindVegIndicator(
    isVeg: Boolean = true,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isVeg) GovindTheme.colors.secondary else Color(0xFF8B2500)
    val dotColor = if (isVeg) GovindTheme.colors.secondary else Color(0xFF8B2500)

    Box(
        modifier = modifier
            .size(size)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(2.dp))
            .padding(2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

// ═════════════════════════════════════════════════════════════
// 2. PRICE DISPLAY
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindPrice(
    price: Double = 0.0,
    mrp: Double? = null,
    sellingPrice: Double = price,
    originalPrice: Double? = mrp,
    unit: String? = null,
    modifier: Modifier = Modifier,
    isDarkBackground: Boolean = false
) {
    val effectivePrice = if (sellingPrice > 0.0) sellingPrice else price
    val effectiveMrp = originalPrice ?: mrp
    val textColor = if (isDarkBackground) Color.White else GovindTheme.colors.textPrimary
    val mutedColor = if (isDarkBackground) Color.White.copy(alpha = 0.7f) else GovindTheme.colors.textMuted

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "₹${effectivePrice.toInt()}",
                style = GovindTheme.priceDisplay,
                color = textColor
            )
            if (effectiveMrp != null && effectiveMrp > effectivePrice) {
                Text(
                    text = "₹${effectiveMrp.toInt()}",
                    style = GovindTheme.priceStrikethrough,
                    color = mutedColor,
                    textDecoration = TextDecoration.LineThrough
                )
            }
        }
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = mutedColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════
// 3. DISCOUNT BADGE
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindDiscountBadge(
    text: String,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false
) {
    val bgColor = if (isSecondary) MaterialTheme.colorScheme.secondaryContainer else GovindTheme.colors.kitchenTint
    val textColor = if (isSecondary) MaterialTheme.colorScheme.onSecondaryContainer else GovindTheme.colors.kitchenAccent

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ═════════════════════════════════════════════════════════════
// 4. DELIVERY ETA PILL
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindDeliveryETA(
    eta: String = "12 Mins",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Bolt,
            contentDescription = null,
            tint = GovindTheme.colors.secondary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = eta,
            style = MaterialTheme.typography.labelSmall,
            color = GovindTheme.colors.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ═════════════════════════════════════════════════════════════
// 5. QUANTITY CONTROL (ADD BUTTON -> STEPPER PILL)
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindQuantityControl(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null
) {
    if (quantity <= 0) {
        // Initial ADD state matching Stitch: rounded-full pill in surfaceContainerLow with primaryContainer text
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 1.dp,
            modifier = modifier
                .defaultMinSize(minWidth = 68.dp, minHeight = 32.dp)
                .clickable { onAdd?.invoke() ?: onIncrement() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ADD",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    } else {
        // Active Stepper state matching Stitch: rounded-full pill in primaryContainer with white text
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp,
            modifier = modifier
                .defaultMinSize(minHeight = 32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                AnimatedContent(
                    targetState = quantity,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                        } else {
                            slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                        }
                    },
                    label = "qty_anim"
                ) { qty ->
                    Text(
                        text = qty.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// 6. STITCH PRODUCT CARD
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    quantityInCart: Int = 0,
    onIncrement: () -> Unit = onAddClick,
    onDecrement: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(Dimens.RadiusCard),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Top Row: Veg indicator + Discount Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GovindVegIndicator(isVeg = true)
                if (product.price != null && product.price > product.sellingPrice) {
                    val discount = ((product.price - product.sellingPrice) / product.price * 100).toInt()
                    GovindDiscountBadge(text = "$discount% OFF")
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }

            // Image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "🥬",
                        fontSize = 42.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // ETA indicator
            GovindDeliveryETA(eta = "12 Mins")

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                color = GovindTheme.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp,
                modifier = Modifier.height(36.dp)
            )

            // Unit
            Text(
                text = product.unit,
                style = MaterialTheme.typography.bodySmall,
                color = GovindTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Row: Price + ADD/Quantity Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (quantityInCart > 0) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "₹${(product.sellingPrice * quantityInCart).toInt()}",
                            style = GovindTheme.priceDisplay,
                            color = GovindTheme.colors.brandPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$quantityInCart in cart • ₹${product.sellingPrice.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = GovindTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    GovindPrice(
                        price = product.sellingPrice,
                        mrp = product.price
                    )
                }

                GovindQuantityControl(
                    quantity = quantityInCart,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    onAdd = onAddClick
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// 7. GOVIND TOP BAR (LOCATION + BRAND + ETA + CART + PROFILE)
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindTopBar(
    locationName: String = "Sector 48, Gurugram",
    eta: String = "12m",
    cartItemCount: Int = 0,
    onLocationClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpaceLg, vertical = Dimens.SpaceSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Logo & Location
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onLocationClick)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "GOVIND EXPRESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = GovindTheme.colors.secondary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GovindTheme.colors.secondary)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = GovindTheme.colors.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.titleSmall,
                            color = GovindTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Select Location",
                            tint = GovindTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Right: ETA Pill + Cart + Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ETA Pill
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            GovindTheme.colors.secondary.copy(alpha = 0.25f)
                        ),
                        shape = CircleShape
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bolt,
                                contentDescription = null,
                                tint = GovindTheme.colors.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = eta,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Cart Icon with Badge
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Cart",
                                tint = GovindTheme.colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            if (cartItemCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-6).dp)
                                        .clip(CircleShape)
                                        .background(GovindTheme.colors.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cartItemCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Profile Icon
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = GovindTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// 8. FLOATING UNIFIED CART DOCK
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindCartDock(
    itemCount: Int,
    totalPrice: Double,
    savingsText: String = "Fresh + Kitchen + Wholesale",
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (itemCount <= 0) return

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = CircleShape,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            GovindTheme.colors.secondary.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Margin, vertical = 6.dp)
            .clickable(onClick = onViewCartClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Count badge + text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GovindTheme.colors.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = "$itemCount Items in Unified Cart",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = savingsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        maxLines = 1
                    )
                }
            }

            // Right: Price + "View Cart ->" pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "₹${totalPrice.toInt()}",
                    style = GovindTheme.priceDisplay,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Surface(
                    color = GovindTheme.colors.secondary,
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "View Cart",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// 9. CIRCULAR CATEGORY ITEM
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindCategoryCircle(
    title: String,
    priceHint: String? = null,
    imageUrl: String? = null,
    iconEmoji: String = "🥬",
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceSm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Text(text = iconEmoji, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = GovindTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (priceHint != null) {
                Text(
                    text = priceHint,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = GovindTheme.colors.secondary,
                    maxLines = 1
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// 10. SECTION HEADER (TITLE + SUBTITLE + SEE ALL)
// ═════════════════════════════════════════════════════════════

@Composable
fun GovindSectionHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.Margin, vertical = Dimens.SpaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = GovindTheme.colors.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = GovindTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = GovindTheme.colors.textMuted
                )
            }
        }

        if (onSeeAllClick != null) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelMedium,
                color = GovindTheme.colors.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onSeeAllClick)
                    .padding(4.dp)
            )
        }
    }
}
