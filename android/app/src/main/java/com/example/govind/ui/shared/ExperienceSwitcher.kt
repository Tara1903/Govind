package com.example.govind.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.govind.theme.GovindTheme
import com.example.govind.ui.navigation.AppState
import com.example.govind.ui.navigation.GovindExperience
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperienceSwitcherDropdown() {
    val currentExperience by AppState.currentExperience.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val title = when (currentExperience) {
        GovindExperience.FRESH -> "Govind Fresh"
        GovindExperience.KITCHEN -> "Govind Kitchen"
        GovindExperience.WHOLESALE -> "Govind Wholesale"
    }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showSheet = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title ▾",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = GovindTheme.colors.govindGreen
            )
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = GovindTheme.colors.pureWhite,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Choose your Govind experience",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GovindTheme.colors.textPrimary,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    ExperienceOption(
                        emoji = "🥬",
                        title = "Govind Fresh",
                        subtitle = "Fresh fruits, vegetables & snacks",
                        isSelected = currentExperience == GovindExperience.FRESH,
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showSheet = false
                                    AppState.switchExperience(GovindExperience.FRESH)
                                }
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExperienceOption(
                        emoji = "🍛",
                        title = "Govind Kitchen",
                        subtitle = "Today's Punjabi food",
                        isSelected = currentExperience == GovindExperience.KITCHEN,
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showSheet = false
                                    AppState.switchExperience(GovindExperience.KITCHEN)
                                }
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExperienceOption(
                        emoji = "📦",
                        title = "Govind Wholesale",
                        subtitle = "Bulk & business orders",
                        isSelected = currentExperience == GovindExperience.WHOLESALE,
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showSheet = false
                                    AppState.switchExperience(GovindExperience.WHOLESALE)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExperienceOption(
    emoji: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) GovindTheme.colors.softGreen else GovindTheme.colors.pureWhite
    val borderColor = if (isSelected) GovindTheme.colors.govindGreen else GovindTheme.colors.border
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = GovindTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = GovindTheme.colors.textSecondary
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = GovindTheme.colors.govindGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
