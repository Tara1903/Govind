package com.example.govind.ui.shared

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.govind.theme.GovindTheme

@Composable
fun GovindQuantityControl(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    isCartTheme: Boolean = false
) {
    val bgColor = if (isCartTheme) GovindTheme.colors.pureWhite else GovindTheme.colors.govindGreen
    val contentColor = if (isCartTheme) GovindTheme.colors.govindGreen else GovindTheme.colors.pureWhite
    val borderColor = GovindTheme.colors.border

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .let {
                if (isCartTheme) it.background(bgColor, RoundedCornerShape(8.dp)) // Maybe border? 
                else it
            }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(28.dp)
        ) {
            Text("-", color = contentColor, fontWeight = FontWeight.Bold)
        }
        AnimatedContent(
            targetState = quantity,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                }
            },
            label = "quantity_animation"
        ) { qty ->
            Text(
                text = qty.toString(),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = contentColor, modifier = Modifier.size(16.dp))
        }
    }
}
