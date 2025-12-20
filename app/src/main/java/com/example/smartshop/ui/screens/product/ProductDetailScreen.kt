package com.example.smartshop.ui.screens.product

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
import androidx.compose.ui.unit.sp
import com.example.smartshop.domain.model.Product
import com.example.smartshop.ui.components.AnimatedStatCard
import com.example.smartshop.ui.theme.PrimaryGradientEnd
import com.example.smartshop.ui.theme.PrimaryGradientStart
import java.util.Locale
@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    "Détails Produit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // PRODUCT INFO CARD
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
                label = "ProductInfo"
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Product Name
                        Column {
                            Text(
                                "Nom du Produit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                product.name ?: "N/A",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Divider()

                        // Product ID
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "ID Produit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    product.id.take(12),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Divider()

                        // Last Updated
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Dernière Mise à Jour",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("fr"))
                                        .format(java.util.Date(product.updatedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // STATS CARDS
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
                label = "Stats"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedStatCard(
                            title = "Quantité Stock",
                            value = product.quantity.toString(),
                            icon = Icons.Default.Inventory2,
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFF10B981),
                            isLoading = false
                        )
                        AnimatedStatCard(
                            title = "Prix Unitaire",
                            value = "$${String.format("%.2f", product.price)}",
                            icon = Icons.Default.AttachMoney,
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFFF59E0B),
                            isLoading = false
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedStatCard(
                            title = "Valeur Totale",
                            value = "$${String.format("%.2f", product.price * product.quantity)}",
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFF0EA5E9),
                            isLoading = false
                        )
                        AnimatedStatCard(
                            title = "Statut Stock",
                            value = when {
                                product.quantity > 50 -> "Élevé"
                                product.quantity > 20 -> "Normal"
                                product.quantity > 5 -> "Faible"
                                else -> "Critique"
                            },
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f),
                            backgroundColor = when {
                                product.quantity > 50 -> Color(0xFF10B981)
                                product.quantity > 20 -> Color(0xFF3B82F6)
                                product.quantity > 5 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            },
                            isLoading = false
                        )
                    }
                }
            }

            // QUANTITY CHART
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
                label = "Chart"
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
                            "Analyse de Stock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Simple Bar Chart Visualization
                        QuantityChartVisualization(product = product)
                    }
                }
            }

            // ACTIONS
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
                label = "Actions"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEdit(product) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGradientStart
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modifier le Produit")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // DELETE CONFIRMATION
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer le produit") },
            text = { Text("Êtes-vous sûr de vouloir supprimer \"${product.name}\"? Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(product.id)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Supprimer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun QuantityChartVisualization(product: Product) {
    val maxQuantity = 100
    val quantityPercentage = (product.quantity.toFloat() / maxQuantity) * 100

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Progress Bar with Label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Stock Actuel",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${product.quantity} / $maxQuantity",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGradientStart
            )
        }

        // Animated Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(quantityPercentage / 100f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                        )
                    )
            )
        }

        Divider()

        // Statistics Breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticItem(
                label = "Min",
                value = "0",
                modifier = Modifier.weight(1f)
            )
            StatisticItem(
                label = "Actuel",
                value = product.quantity.toString(),
                modifier = Modifier.weight(1f)
            )
            StatisticItem(
                label = "Max",
                value = maxQuantity.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Divider()

        // Health Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryGradientStart.copy(alpha = 0.1f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when {
                    product.quantity > 50 -> Icons.Default.CheckCircle
                    product.quantity > 20 -> Icons.Default.Info
                    product.quantity > 5 -> Icons.Default.Warning
                    else -> Icons.Default.ErrorOutline
                },
                contentDescription = null,
                tint = when {
                    product.quantity > 50 -> Color(0xFF10B981)
                    product.quantity > 20 -> Color(0xFF3B82F6)
                    product.quantity > 5 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                },
                modifier = Modifier.size(20.dp)
            )
            Text(
                when {
                    product.quantity > 50 -> "Stock Abondant - Tout va bien"
                    product.quantity > 20 -> "Stock Normal - Situation stable"
                    product.quantity > 5 -> "Stock Faible - À surveiller"
                    else -> "Stock Critique - Action requise"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}