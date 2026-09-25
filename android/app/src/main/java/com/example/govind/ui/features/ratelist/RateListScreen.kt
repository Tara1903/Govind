package com.example.govind.ui.features.ratelist

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class RateItem(val name: String, val unit: String, val price: String)
data class RateSection(val title: String, val items: List<RateItem>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateListScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    val rateSections = listOf(
        RateSection(
            title = "Fresh Vegetables",
            items = listOf(
                RateItem("Tomato", "1 kg", "\u20B935"),
                RateItem("Potato", "1 kg", "\u20B942"),
                RateItem("Onion", "1 kg", "\u20B948"),
                RateItem("Methi", "1 bunch", "\u20B930"),
                RateItem("Coriander", "1 bunch", "\u20B915"),
                RateItem("Green Chilli", "1 kg", "\u20B960")
            )
        ),
        RateSection(
            title = "Fresh Fruits",
            items = listOf(
                RateItem("Apple", "1 kg", "\u20B9180"),
                RateItem("Banana", "1 dozen", "\u20B940"),
                RateItem("Orange", "1 kg", "\u20B9120")
            )
        ),
        RateSection(
            title = "Healthy Snacks",
            items = listOf(
                RateItem("Roasted Makhana", "200g", "\u20B9150"),
                RateItem("Mixed Nuts", "500g", "\u20B9450")
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Rate List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFEFCF5),
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFFFEFCF5),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        val url = "https://wa.me/919630937033?text=Hi,%20I%20want%20today's%20fresh%20rate%20list."
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Get Rates on WhatsApp",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        containerColor = Color(0xFFFEFCF5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Updated 10:42 AM",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            rateSections.forEach { section ->
                item {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF064520),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                
                items(section.items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = item.unit,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = item.price,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}
