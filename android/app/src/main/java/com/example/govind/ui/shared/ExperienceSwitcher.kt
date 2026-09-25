package com.example.govind.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.govind.ui.navigation.AppState
import com.example.govind.ui.navigation.GovindExperience
import androidx.navigation.NavController
import com.example.govind.ui.navigation.Screen

@Composable
fun ExperienceSwitcherHeader(
    navController: NavController
) {
    val currentExperience by AppState.currentExperience.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val (title, color, desc) = when (currentExperience) {
        GovindExperience.FRESH -> Triple("Govind Fresh", Color(0xFF38802A), "Fresh fruits, vegetables & snacks")
        GovindExperience.KITCHEN -> Triple("Govind Kitchen", Color(0xFFF5450D), "Today's Punjabi food")
        GovindExperience.WHOLESALE -> Triple("Govind Wholesale", Color(0xFF064520), "Bulk & business orders")
    }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch Experience",
                        tint = color
                    )
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("🥬 Govind Fresh", fontWeight = FontWeight.Bold, color = Color(0xFF38802A))
                        Text("Fresh fruits, vegetables & snacks", style = MaterialTheme.typography.labelSmall)
                    }
                },
                onClick = {
                    expanded = false
                    AppState.switchExperience(GovindExperience.FRESH)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("🍛 Govind Kitchen", fontWeight = FontWeight.Bold, color = Color(0xFFF5450D))
                        Text("Today's Punjabi food", style = MaterialTheme.typography.labelSmall)
                    }
                },
                onClick = {
                    expanded = false
                    AppState.switchExperience(GovindExperience.KITCHEN)
                    navController.navigate(Screen.KitchenHome.route) {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("📦 Govind Wholesale", fontWeight = FontWeight.Bold, color = Color(0xFF064520))
                        Text("Bulk & business orders", style = MaterialTheme.typography.labelSmall)
                    }
                },
                onClick = {
                    expanded = false
                    AppState.switchExperience(GovindExperience.WHOLESALE)
                    navController.navigate(Screen.WholesaleHome.route) {
                        popUpTo(navController.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
