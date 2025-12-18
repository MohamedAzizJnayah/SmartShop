package com.example.smartshop.ui.screens.home

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartshop.domain.model.Product
import com.example.smartshop.ui.viewmodel.authentication.AuthViewModel
import com.example.smartshop.ui.viewmodel.product.ProductViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private enum class ChartType { BAR, PIE }
private enum class ChartMetric { QUANTITY, VALUE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    productViewModel: ProductViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user by authViewModel.user.collectAsState()
    val ui by productViewModel.state.collectAsState()

    // anti flash login
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

    // chart state
    var chartType by remember { mutableStateOf(ChartType.BAR) }
    var chartMetric by remember { mutableStateOf(ChartMetric.QUANTITY) }

    if (!authChecked) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // ========= EXPORT (CSV / PDF) =========
    fun nowFileStamp(): String =
        SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val csv = productsToCsv(filtered)
                writeTextToUri(context, uri, csv)
            }.onSuccess {
                snackbar.showSnackbar("CSV exporté ✅")
            }.onFailure {
                snackbar.showSnackbar("Erreur export CSV: ${it.message}")
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                writeProductsPdf(context, uri, filtered)
            }.onSuccess {
                snackbar.showSnackbar("PDF exporté ✅")
            }.onFailure {
                snackbar.showSnackbar("Erreur export PDF: ${it.message}")
            }
        }
    }

    // dialogs
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

    // export menu
    var exportMenu by remember { mutableStateOf(false) }

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

                    IconButton(onClick = { exportMenu = true }) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Export")
                    }
                    DropdownMenu(expanded = exportMenu, onDismissRequest = { exportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Exporter CSV") },
                            onClick = {
                                exportMenu = false
                                csvLauncher.launch("smartshop_products_${nowFileStamp()}.csv")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Exporter PDF") },
                            onClick = {
                                exportMenu = false
                                pdfLauncher.launch("smartshop_products_${nowFileStamp()}.pdf")
                            }
                        )
                    }

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
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(56.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text("Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Produits: ${ui.stats.totalProducts}  •  Valeur: ${formatMoney(ui.stats.totalStockValue)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (ui.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // ===== CHART CARD =====
            item {
                ChartCard(
                    products = filtered,
                    chartType = chartType,
                    metric = chartMetric,
                    onTypeChange = { chartType = it },
                    onMetricChange = { chartMetric = it }
                )
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
                    AssistChip(onClick = {}, label = { Text("${filtered.size}") })
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
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}

// ===================== CHART UI =====================

@Composable
private fun ChartCard(
    products: List<Product>,
    chartType: ChartType,
    metric: ChartMetric,
    onTypeChange: (ChartType) -> Unit,
    onMetricChange: (ChartMetric) -> Unit
) {
    val data by remember(products, metric) {
        derivedStateOf {
            val sorted = when (metric) {
                ChartMetric.QUANTITY -> products.sortedByDescending { it.quantity ?: 0 }
                ChartMetric.VALUE -> products.sortedByDescending { (it.price ?: 0.0) * (it.quantity ?: 0) }
            }
            val top = sorted.take(5)
            top.map {
                val v = when (metric) {
                    ChartMetric.QUANTITY -> (it.quantity ?: 0).toFloat()
                    ChartMetric.VALUE -> ((it.price ?: 0.0) * (it.quantity ?: 0)).toFloat()
                }
                (it.name ?: "N/A") to v
            }
        }
    }

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Visualisation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { onTypeChange(if (chartType == ChartType.BAR) ChartType.PIE else ChartType.BAR) },
                    label = { Text(if (chartType == ChartType.BAR) "Bar" else "Pie") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = metric == ChartMetric.QUANTITY,
                    onClick = { onMetricChange(ChartMetric.QUANTITY) },
                    label = { Text("Quantité") }
                )
                FilterChip(
                    selected = metric == ChartMetric.VALUE,
                    onClick = { onMetricChange(ChartMetric.VALUE) },
                    label = { Text("Valeur") }
                )
            }

            if (products.isEmpty()) {
                Text("Aucune donnée pour le graphique.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                when (chartType) {
                    ChartType.BAR -> BarChart(data = data, metric = metric)
                    ChartType.PIE -> PieChart(data = data, metric = metric)
                }
            }

            Text(
                "Top 5 produits (selon ${if (metric == ChartMetric.QUANTITY) "quantité" else "valeur"}).",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<Pair<String, Float>>,
    metric: ChartMetric
) {
    val max = (data.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        data.forEach { (label, value) ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (metric == ChartMetric.QUANTITY) value.toInt().toString()
                        else formatMoney(value.toDouble()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((value / max).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    data: List<Pair<String, Float>>,
    metric: ChartMetric
) {
    val total = data.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )

    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var start = -90f
            data.forEachIndexed { index, (_, v) ->
                val sweep = (v / total) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true
                )
                start += sweep
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            data.forEachIndexed { index, (label, v) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors[index % colors.size])
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        label,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (metric == ChartMetric.QUANTITY) v.toInt().toString()
                        else formatMoney(v.toDouble()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ===================== PRODUCT ROW + EDITOR =====================

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
                    FilledTonalIconButton(onClick = onEdit, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    FilledTonalIconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim(), qty.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ===================== EXPORT HELPERS (CSV / PDF) =====================

private fun productsToCsv(products: List<Product>): String {
    val sb = StringBuilder()
    sb.append("id,name,quantity,price,total\n")
    products.forEach { p ->
        val name = (p.name ?: "").replace(",", " ")
        val qty = p.quantity ?: 0
        val price = p.price ?: 0.0
        val total = price * qty
        sb.append("${p.id},$name,$qty,$price,$total\n")
    }
    return sb.toString()
}

private fun writeTextToUri(context: Context, uri: android.net.Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { out ->
        out.write(text.toByteArray(Charsets.UTF_8))
        out.flush()
    } ?: error("Impossible d’ouvrir le fichier")
}

private fun writeProductsPdf(context: Context, uri: android.net.Uri, products: List<Product>) {
    val doc = PdfDocument()

    val pageW = 595  // A4 approx
    val pageH = 842
    val margin = 40

    val titlePaint = Paint().apply {
        isAntiAlias = true
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.BLACK
    }
    val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        color = android.graphics.Color.DKGRAY
    }
    val headerPaint = Paint().apply {
        isAntiAlias = true
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.BLACK
    }

    var pageNumber = 1
    var y = margin

    fun newPage(): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNumber).create()
        val page = doc.startPage(pageInfo)
        y = margin

        page.canvas.drawText("SmartShop - Liste des produits", margin.toFloat(), y.toFloat(), titlePaint)
        y += 26

        // header row
        page.canvas.drawText("Nom", margin.toFloat(), y.toFloat(), headerPaint)
        page.canvas.drawText("Qté", (margin + 260).toFloat(), y.toFloat(), headerPaint)
        page.canvas.drawText("Prix", (margin + 330).toFloat(), y.toFloat(), headerPaint)
        page.canvas.drawText("Total", (margin + 420).toFloat(), y.toFloat(), headerPaint)
        y += 16

        return page
    }

    var page = newPage()

    var grandTotal = 0.0
    products.forEach { p ->
        val name = p.name ?: "Sans nom"
        val qty = p.quantity ?: 0
        val price = p.price ?: 0.0
        val total = price * qty
        grandTotal += total

        if (y > pageH - margin) {
            doc.finishPage(page)
            pageNumber++
            page = newPage()
        }

        page.canvas.drawText(name.take(28), margin.toFloat(), y.toFloat(), textPaint)
        page.canvas.drawText(qty.toString(), (margin + 260).toFloat(), y.toFloat(), textPaint)
        page.canvas.drawText(String.format(Locale.getDefault(), "%.2f", price), (margin + 330).toFloat(), y.toFloat(), textPaint)
        page.canvas.drawText(String.format(Locale.getDefault(), "%.2f", total), (margin + 420).toFloat(), y.toFloat(), textPaint)
        y += 16
    }

    // footer total
    if (y > pageH - margin) {
        doc.finishPage(page)
        pageNumber++
        page = newPage()
    }
    y += 10
    page.canvas.drawText("Total stock: ${String.format(Locale.getDefault(), "%.2f", grandTotal)} DT", margin.toFloat(), y.toFloat(), headerPaint)

    doc.finishPage(page)

    context.contentResolver.openOutputStream(uri)?.use { out ->
        doc.writeTo(out)
        out.flush()
    } ?: error("Impossible d’ouvrir le fichier PDF")

    doc.close()
}

// ===================== FORMAT =====================

private fun formatMoney(value: Double): String {
    val rounded = ((value * 100).roundToInt() / 100.0)
    return "$rounded DT"
}
