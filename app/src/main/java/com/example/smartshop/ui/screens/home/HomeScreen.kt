package com.example.smartshop.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackbar.showSnackbar(it)
            productViewModel.clearError()
        }
    }

    var query by remember { mutableStateOf("") }
    val filtered by remember(query, ui.products) {
        derivedStateOf {
            val q = query.trim()
            if (q.isBlank()) ui.products
            else ui.products.filter { (it.name ?: "").contains(q, ignoreCase = true) }
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var askDelete by remember { mutableStateOf<Product?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Product?>(null) }

    if (!authChecked) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

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
                Button(
                    onClick = {
                        askDelete = null
                        productViewModel.deleteProduct(p.id)
                        scope.launch { snackbar.showSnackbar("Produit supprimé") }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { askDelete = null }) { Text("Annuler") } }
        )
    }

    if (showEditor) {
        ProductEditorDialog(
            initial = editing,
            onDismiss = { showEditor = false; editing = null },
            onSave = { name, qty, price ->
                if (editing == null) productViewModel.addProduct(name, qty, price)
                else productViewModel.updateProduct(editing!!.copy(name = name, quantity = qty, price = price))
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showEditor = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ajouter") }
            )
        },
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column {
                        Text("SmartShop", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    val initial = user?.email?.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = {
                            productViewModel.syncNow(clearLocalFirst = false)
                            scope.launch { snackbar.showSnackbar("Sync demandé…") }
                        }
                    ) { Icon(Icons.Default.Sync, contentDescription = "Sync") }

                    IconButton(onClick = { showLogoutDialog = true }) {
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ===== DASHBOARD CARD =====
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // accent bar
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(56.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )

                        Text("Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCardPro(
                                title = "Produits",
                                value = ui.stats.totalProducts.toString(),
                                icon = Icons.Default.Inventory2,
                                modifier = Modifier.weight(1f)
                            )
                            StatCardPro(
                                title = "Valeur stock",
                                value = formatMoney(ui.stats.totalStockValue),
                                icon = Icons.Default.Payments,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (ui.isLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // ===== SEARCH =====
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (query.isNotBlank()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                                IconButton(onClick = { /* filtre plus tard */ }) {
                                    Icon(Icons.Default.Tune, contentDescription = "Filter")
                                }
                            }
                        },
                        placeholder = { Text("Rechercher un produit...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ===== HEADER LIST =====
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Produits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = { /* tri plus tard */ },
                        label = { Text("${filtered.size}") }
                    )
                }
            }

            // ===== LIST / EMPTY =====
            if (!ui.isLoading && filtered.isEmpty()) {
                item {
                    OutlinedCard(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Aucun produit", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Ajoute ton premier produit ou change la recherche.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { p ->
                    ProductRowCardPro(
                        product = p,
                        onEdit = { editing = p; showEditor = true },
                        onDelete = { askDelete = p }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) } // espace pour le FAB
            }
        }
    }
}

@Composable
private fun StatCardPro(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        modifier = modifier.height(98.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(Modifier.padding(10.dp)) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProductRowCardPro(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (product.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "P"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            headlineContent = {
                Text(
                    product.name ?: "Sans nom",
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    "Qté: ${product.quantity ?: 0} • ${formatMoney(product.price ?: 0.0)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(38.dp)
                    ) { Icon(Icons.Default.Edit, contentDescription = "Edit") }

                    FilledTonalIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                }
            }
        )
    }
}

@Composable
private fun ProductEditorDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, qty: Int, price: Double) -> Unit
) {
    // ✅ important: reset quand tu changes de produit
    val key = initial?.id ?: "new"
    var name by remember(key) { mutableStateOf(initial?.name.orEmpty()) }
    var qty by remember(key) { mutableStateOf((initial?.quantity ?: 0).toString()) }
    var price by remember(key) { mutableStateOf((initial?.price ?: 0.0).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Ajouter produit" else "Modifier produit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantité") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Prix") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                )
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
