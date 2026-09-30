package com.example.govind.ui.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.govind.theme.Dimens
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.navigation.AppState
import com.example.govind.ui.navigation.GovindExperience

/**
 * 3-Way Experience Switcher with min 44dp height matching Stitch design.
 * Features pill track with surfaceContainerHigh background, 1dp outline-variant border,
 * active primary-container fill with green indicator pulse dot, and inactive surface-container-low pills.
 */
@Composable
fun PillExperienceSwitcher(
    modifier: Modifier = Modifier
) {
    val currentExperience by AppState.currentExperience.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExperiencePill(
                title = "Fresh",
                icon = Icons.Outlined.Eco,
                isSelected = currentExperience == GovindExperience.FRESH,
                activeIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { AppState.switchExperience(GovindExperience.FRESH) },
                modifier = Modifier.weight(1f)
            )
            ExperiencePill(
                title = "Kitchen",
                icon = Icons.Outlined.Restaurant,
                isSelected = currentExperience == GovindExperience.KITCHEN,
                activeIndicatorColor = GovindTheme.colors.kitchenAccent,
                onClick = { AppState.switchExperience(GovindExperience.KITCHEN) },
                modifier = Modifier.weight(1f)
            )
            ExperiencePill(
                title = "Wholesale",
                icon = Icons.Outlined.Inventory2,
                isSelected = currentExperience == GovindExperience.WHOLESALE,
                activeIndicatorColor = GovindTheme.colors.wholesaleAmber,
                onClick = { AppState.switchExperience(GovindExperience.WHOLESALE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ExperiencePill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeIndicatorColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetBgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val targetContentColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val animatedBg by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = 200),
        label = "pill_bg"
    )
    val animatedContent by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = tween(durationMillis = 200),
        label = "pill_content"
    )

    Surface(
        color = animatedBg,
        shape = CircleShape,
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) activeIndicatorColor else animatedContent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = animatedContent,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
            }

            // Small active indicator dot (as in Stitch)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(activeIndicatorColor)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}
