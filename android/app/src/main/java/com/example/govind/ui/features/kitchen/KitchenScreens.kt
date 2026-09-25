package com.example.govind.ui.features.kitchen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val category: String,
    val emoji: String = "\uD83C\uDF72"
)

val kitchenMenuData = listOf(
    MenuItem("1", "Rajma Chawal", "Comforting kidney beans curry with rice", 89, "Meals", "\uD83C\uDF7B"),
    MenuItem("2", "Dal Makhani", "Creamy and buttery black lentils", 79, "Meals", "\uD83C\uDF72"),
    MenuItem("3", "Chole Bhature", "Spicy chickpea curry with fried bread", 99, "Meals", "\uD83E\uDDF6"),
    MenuItem("4", "Aloo Paratha", "Potato stuffed flatbread with butter", 49, "Paratha", "\uD83E\uDED3"),
    MenuItem("5", "Paneer Paratha", "Cottage cheese stuffed flatbread", 69, "Paratha", "\uD83E\uDED3"),
    MenuItem("6", "Special Thali", "Complete meal with dal, paneer, rice, roti, and sweet", 149, "Thali", "\uD83C\uDF71"),
    MenuItem("7", "Raita", "Cooling yogurt with cucumber and spices", 29, "Sides", "\uD83E\uDD63"),
    MenuItem("8", "Butter Naan", "Soft flatbread cooked in tandoor with butter", 20, "Sides", "\uD83E\uDDF3")
)

val kitchenAccentColor = Color(0xFFF5450D)
val warmBackgroundColor = Color(0xFFFFF8F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenHomeScreen() {
    val scrollState = rememberScrollState()
    val today = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Meals", "Paratha", "Thali", "Sides")
    
    val filteredItems = if (selectedCategory == "All") {
        kitchenMenuData
    } else {
        kitchenMenuData.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.govind.theme.GovindTheme.colors.softCream)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "GOVIND KITCHEN",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = kitchenAccentColor,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = today,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Hero Section - Today's Special
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = warmBackgroundColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = kitchenAccentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "TODAY'S SPECIAL",
                            color = kitchenAccentColor,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Rajma Chawal",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹89",
                        style = MaterialTheme.typography.titleLarge,
                        color = kitchenAccentColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Add to cart */ },
                        colors = ButtonDefaults.buttonColors(containerColor = kitchenAccentColor)
                    ) {
                        Text("Add to Cart")
                    }
                }
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(Color(0xFFFFE0D2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("\uD83C\uDF7B", fontSize = 60.sp)
                }
            }
        }

        // Popular Today
        Text(
            text = "Popular Today",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            items(kitchenMenuData.take(4)) { item ->
                PopularItemCard(item)
            }
        }

        // Category Chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {},
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            categories.forEach { category ->
                val selected = selectedCategory == category
                Surface(
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) kitchenAccentColor else Color(0xFFF5F5F5),
                    onClick = { selectedCategory = category }
                ) {
                    Text(
                        text = category,
                        color = if (selected) Color.White else Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        // Menu Items List
        filteredItems.forEach { item ->
            KitchenMenuItem(item)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PopularItemCard(item: MenuItem) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = warmBackgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFD4C4)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "₹${item.price}",
                    style = MaterialTheme.typography.titleMedium,
                    color = kitchenAccentColor
                )
                IconButton(
                    onClick = { /* Add */ },
                    modifier = Modifier
                        .size(32.dp)
                        .background(kitchenAccentColor, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun KitchenMenuItem(item: MenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(com.example.govind.theme.GovindTheme.colors.softCream)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image Placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(warmBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, fontSize = 50.sp)
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "₹${item.price}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = { /* Add */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = kitchenAccentColor
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, kitchenAccentColor)
                ) {
                    Text("ADD", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenMenuScreen() {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Meals", "Paratha", "Thali", "Sides")
    
    val groupedItems = if (selectedCategory == "All") {
        kitchenMenuData.groupBy { it.category }
    } else {
        kitchenMenuData.filter { it.category == selectedCategory }.groupBy { it.category }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.govind.theme.GovindTheme.colors.softCream)
    ) {
        // App Bar
        TopAppBar(
            title = { Text("Govind Menu", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = com.example.govind.theme.GovindTheme.colors.softCream,
                titleContentColor = kitchenAccentColor
            )
        )

        // Filter Chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0,
            edgePadding = 16.dp,
            containerColor = com.example.govind.theme.GovindTheme.colors.softCream,
            divider = {},
            indicator = {},
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            categories.forEach { category ->
                val selected = selectedCategory == category
                Surface(
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) kitchenAccentColor else Color(0xFFF5F5F5),
                    onClick = { selectedCategory = category }
                ) {
                    Text(
                        text = category,
                        color = if (selected) Color.White else Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        // Menu List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            groupedItems.forEach { (category, items) ->
                item {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(items) { item ->
                    LargeMenuCard(item)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun LargeMenuCard(item: MenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = warmBackgroundColor)
    ) {
        Column {
            // Large Image Placeholder (180dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFFFD4C4)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 80.sp)
            }
            
            // Content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${item.price}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = kitchenAccentColor
                    )
                }
                Button(
                    onClick = { /* Add */ },
                    colors = ButtonDefaults.buttonColors(containerColor = kitchenAccentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ADD", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
