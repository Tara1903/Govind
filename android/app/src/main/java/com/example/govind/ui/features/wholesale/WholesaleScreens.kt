package com.example.govind.ui.features.wholesale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.govind.theme.GovindTheme

@Composable
fun WholesaleHomeScreen() {
    WholesaleEmptyState()
}

@Composable
fun WholesaleCatalogScreen() {
    WholesaleEmptyState()
}

@Composable
fun WholesaleEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GovindTheme.colors.warmWhite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GovindTheme.colors.softGreen,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Business",
                        tint = GovindTheme.colors.govindGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "GOVIND WHOLESALE",
                style = MaterialTheme.typography.labelLarge,
                color = GovindTheme.colors.freshGreen,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = androidx.compose.ui.unit.TextUnit(2f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Fresh produce for your business.",
                style = MaterialTheme.typography.headlineSmall,
                color = GovindTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Get daily rates and bulk quotes for restaurants, caterers, and retailers.",
                style = MaterialTheme.typography.bodyLarge,
                color = GovindTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovindTheme.colors.govindGreen)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Request a Bulk Quote", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GovindTheme.colors.govindGreen),
                border = androidx.compose.foundation.BorderStroke(1.dp, GovindTheme.colors.govindGreen)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Govind", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
