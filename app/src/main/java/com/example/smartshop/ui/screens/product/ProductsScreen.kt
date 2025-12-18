package com.example.smartshop.ui.screens.product

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartshop.domain.model.Product
import com.example.smartshop.ui.viewmodel.product.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(vm: ProductViewModel) {
    val state by vm.state.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Produits", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(8.dp))
            Text("Total: ${state.stats.totalProducts}  |  Valeur: ${"%.2f".format(state.stats.totalStockValue)}")

            Spacer(Modifier.height(12.dp))

            state.error?.let { msg ->
                AssistChip(
                    onClick = { vm.clearError() },
                    label = { Text(msg) }
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.products, key = { it.id }) { p ->
                        ProductCard(
                            product = p,
                            onEdit = {
                                editing = p
                                showDialog = true
                            },
                            onDelete = { vm.deleteProduct(p.id) }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddEditProductDialog(
                initial = editing,
                onDismiss = { showDialog = false },
                onConfirm = { name, qty, price ->
                    if (editing == null) vm.addProduct(name, qty, price)
                    else vm.updateProduct(editing!!.copy(name = name, quantity = qty, price = price))
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Qty: ${product.quantity} | Prix: ${product.price}")
                Text("Total: ${"%.2f".format(product.quantity * product.price)}")
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

private fun AddEditProductDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, qty: Int, price: Double) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var qtyText by remember { mutableStateOf(initial?.quantity?.toString() ?: "0") }
    var priceText by remember { mutableStateOf(initial?.price?.toString() ?: "0.0") }

    // validation simple UI
    val qty = qtyText.toIntOrNull()
    val price = priceText.toDoubleOrNull()
    val canSave = name.isNotBlank() && qty != null && qty >= 0 && price != null && price > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Ajouter un produit" else "Modifier le produit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Prix") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                if (!canSave) {
                    Text("Vérifie: nom, quantité ≥ 0, prix > 0", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onConfirm(name.trim(), qty!!, price!!) }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
