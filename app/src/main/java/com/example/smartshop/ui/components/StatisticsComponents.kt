package com.example.smartshop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartshop.ui.theme.PrimaryGradientEnd
import com.example.smartshop.ui.theme.PrimaryGradientStart
import androidx.compose.ui.graphics.Color as ComposeColor

// 1. STAT CARD WITH ANIMATION
@Composable
fun AnimatedStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: ComposeColor = PrimaryGradientStart,
    isLoading: Boolean = false
) {
    val scale: Float by animateFloatAsState(
        targetValue = if (isLoading) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "CardScale"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelMedium,
                        color = ComposeColor.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = ComposeColor.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = ComposeColor.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}