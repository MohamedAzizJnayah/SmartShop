package com.example.smartshop.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartshop.domain.model.Product
import com.example.smartshop.ui.viewmodel.authentication.AuthViewModel
import com.example.smartshop.ui.viewmodel.product.ProductViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    productViewModel: ProductViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val user by authViewModel.user.collectAsState()
    val ui by productViewModel.state.collectAsState()

    var authChecked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(350); authChecked = true }
    LaunchedEffect(user, authChecked) { if (authChecked && user == null) onLogout() }

    if (!authChecked) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ✅ afficher erreur VM
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbar.showSnackbar(it)
            productViewModel.clearError()
        }
    }

    var query by remember { mutableStateOf("") }
    val filtered = remember(query, ui.products) {
        if (query.isBlank()) ui.products
        else ui.products.filter { (it.name ?: "").contains(query.trim(), ignoreCase = true) }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var askDelete by remember { mutableStateOf<Product?>(null) }

    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Product?>(null) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion") },
            text = { Text("Tu es sûre de vouloir te déconnecter ?") },
            confirmButton = {
                Button(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                    onLogout()
                }) { Text("Se déconnecter") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") } }
        )
    }

    askDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { askDelete = null },
            title = { Text("Supprimer") },
            text = { Text("Supprimer “${p.name ?: "Produit"}” ?") },
            confirmButton = {
                Button(onClick = {
                    askDelete = null
                    productViewModel.deleteProduct(p.id)
                    scope.launch { snackbar.showSnackbar("Produit supprimé") }
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { askDelete = null }) { Text("Annuler") } }
        )
    }

    if (showEditor) {
        ProductEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false; editing = null },
            onSave = { name, qty, price ->
                if (editing == null) {
                    productViewModel.addProduct(name, qty, price)
                } else {
                    productViewModel.updateProduct(editing!!.copy(name = name, quantity = qty, price = price))
                }
                showEditor = false
                editing = null
                scope.launch { snackbar.showSnackbar("Enregistré ✅") }
            }
        )
    }

    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SmartShop", fontWeight = FontWeight.SemiBold)
                        Text(
                            user?.email ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        productViewModel.syncNow(clearLocalFirst = false)
                        scope.launch { snackbar.showSnackbar("Sync demandé…") }
                    }) { Icon(Icons.Default.Sync, contentDescription = "Sync") }

                    FilledTonalIconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Produits: ${ui.stats.totalProducts}  •  Valeur: ${formatMoney(ui.stats.totalStockValue)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Rechercher un produit...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Produits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            if (ui.isLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            } else if (filtered.isEmpty()) {
                item {
                    OutlinedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Aucun produit", fontWeight = FontWeight.SemiBold)
                            Text("Ajoute ton premier produit ou change la recherche.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { p ->
                    ProductRowCard(
                        product = p,
                        onEdit = { editing = p; showEditor = true },
                        onDelete = { askDelete = p }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductRowCard(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(product.name ?: "Sans nom", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "Qté: ${product.quantity ?: 0} • ${formatMoney(product.price ?: 0.0)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@Composable
private fun ProductEditorDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, qty: Int, price: Double) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var qty by remember { mutableStateOf((initial?.quantity ?: 0).toString()) }
    var price by remember { mutableStateOf((initial?.price ?: 0.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Ajouter produit" else "Modifier produit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, singleLine = true)
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantité") }, singleLine = true)
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Prix") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(name.trim(), qty.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0)
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

private fun formatMoney(value: Double): String {
    val rounded = ((value * 100).roundToInt() / 100.0)
    return "$rounded DT"
}
