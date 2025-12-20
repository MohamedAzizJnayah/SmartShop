

package com.example.smartshop.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartshop.domain.model.Product
import com.example.smartshop.ui.components.AnimatedStatCard
import com.example.smartshop.ui.theme.PrimaryGradientEnd
import com.example.smartshop.ui.theme.PrimaryGradientStart

@Composable
fun StatisticsScreen(
    products: List<Product>,
    userName: String = "User",
    isLoading: Boolean = false
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val totalProducts = products.size
    val totalValue = products.sumOf { (it.price ?: 0.0) * (it.quantity ?: 0) }
    val totalQuantity = products.sumOf { it.quantity ?: 0 }
    val avgPrice = if (products.isNotEmpty()) products.map { it.price ?: 0.0 }.average() else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // HEADER WITH GREETING
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn(),
            label = "Header"
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Tableau de Bord",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Bienvenue, $userName! 👋",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // STATS CARDS - STAGGERED ANIMATION
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
            label = "Stats"
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Products & Quantity
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedStatCard(
                        title = "Total Produits",
                        value = totalProducts.toString(),
                        icon = Icons.Default.ShoppingCart,
                        modifier = Modifier.weight(1f),
                        backgroundColor = PrimaryGradientStart,
                        isLoading = isLoading
                    )
                    AnimatedStatCard(
                        title = "Quantité Stock",
                        value = totalQuantity.toString(),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f),
                        backgroundColor = Color(0xFF10B981),
                        isLoading = isLoading
                    )
                }

                // Row 2: Total Value & Average Price
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedStatCard(
                        title = "Valeur Totale",
                        value = "$${String.format("%.2f", totalValue)}",
                        icon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f),
                        backgroundColor = Color(0xFFF59E0B),
                        isLoading = isLoading
                    )
                    AnimatedStatCard(
                        title = "Prix Moyen",
                        value = "$${String.format("%.2f", avgPrice)}",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f),
                        backgroundColor = Color(0xFF0EA5E9),
                        isLoading = isLoading
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // QUICK STATS SECTION
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
            label = "QuickStats"
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Statistiques Rapides",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    QuickStatRow(
                        label = "Produit le plus cher",
                        value = if (products.isNotEmpty()) {
                            "$${String.format("%.2f", products.maxByOrNull { it.price ?: 0.0 }?.price ?: 0.0)}"
                        } else "N/A"
                    )

                    Divider()

                    QuickStatRow(
                        label = "Produit le moins cher",
                        value = if (products.isNotEmpty()) {
                            "$${String.format("%.2f", products.minByOrNull { it.price ?: 0.0 }?.price ?: 0.0)}"
                        } else "N/A"
                    )

                    Divider()

                    QuickStatRow(
                        label = "Stock faible (<5)",
                        value = products.count { (it.quantity ?: 0) < 5 }.toString()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun QuickStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}