package com.tasker.chronos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasker.chronos.data.models.ShoppingItem
import com.tasker.chronos.data.models.ShoppingCategory
import com.tasker.chronos.ui.components.GlassCard
import com.tasker.chronos.viewmodels.ShoppingViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    shoppingViewModel: ShoppingViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val items by shoppingViewModel.items.collectAsState()
    val categories by shoppingViewModel.categories.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyUnpurchased by remember { mutableStateOf(true) }
    var budgetText by remember { mutableStateOf("") }
    var calcSelectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var calcMode by remember { mutableStateOf("filter") } // filter | selected | all
    val budget = budgetText.toDoubleOrNull()

    val filteredItems = remember(items, selectedCategoryFilter, showOnlyUnpurchased) {
        items.filter { item ->
            val categoryMatch = item.belongsToCategory(selectedCategoryFilter)
            val purchasedMatch = !showOnlyUnpurchased || !item.isPurchased
            categoryMatch && purchasedMatch
        }.sortedWith(
            compareBy<ShoppingItem> { it.isPurchased }
                .thenByDescending { it.createdAt }
        )
    }

    val calcItems = remember(items, filteredItems, calcSelectedIds, calcMode, selectedCategoryFilter) {
        when (calcMode) {
            "selected" -> items.filter { it.id in calcSelectedIds && !it.isPurchased }
            "filter" -> filteredItems.filter { !it.isPurchased }
            else -> items.filter { !it.isPurchased }
        }
    }
    val calcTotal = calcItems.sumOf { it.price }
    val purchasedTotal = shoppingViewModel.getPurchasedTotal()
    val remaining = budget?.let { it - calcTotal }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🛒 Lista Zakupów",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Category, "Kategorie")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Dodaj zakup")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Statystyki + kalkulator budżetu
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Do kupienia (kalkulator):",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${String.format("%.2f", calcTotal)} zł",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                when (calcMode) {
                                    "selected" -> "Zaznaczone: ${calcItems.size} szt."
                                    "filter" -> if (selectedCategoryFilter != null) "Wg filtra kategorii" else "Lista widoczna"
                                    else -> "Wszystkie niekupione"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Zakupione:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${String.format("%.2f", purchasedTotal)} zł",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = calcMode == "filter",
                            onClick = { calcMode = "filter" },
                            label = { Text("Filtr") }
                        )
                        FilterChip(
                            selected = calcMode == "selected",
                            onClick = { calcMode = "selected" },
                            label = { Text("Zaznaczone") }
                        )
                        FilterChip(
                            selected = calcMode == "all",
                            onClick = { calcMode = "all" },
                            label = { Text("Wszystkie") }
                        )
                    }

                    if (calcMode == "selected") {
                        Text(
                            "Zaznacz produkty ikona kalkulatora na karcie.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (calcSelectedIds.isNotEmpty()) {
                            TextButton(onClick = { calcSelectedIds = emptySet() }) {
                                Text("Wyczysc zaznaczenie (${calcSelectedIds.size})")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                budgetText = it
                            }
                        },
                        label = { Text("Moj budzet (ile mam pieniedzy)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("zl") },
                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                    )

                    if (remaining != null) {
                        val ok = remaining >= 0
                        Text(
                            text = if (ok)
                                "Zostanie: ${String.format("%.2f", remaining)} zl"
                            else
                                "Brakuje: ${String.format("%.2f", -remaining)} zl",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (ok) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Filtry
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Toggle pokazywania kupionych
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pokaż zakupione",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = !showOnlyUnpurchased,
                        onCheckedChange = { showOnlyUnpurchased = !it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtry kategorii
                if (categories.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("Wszystkie (${items.size})") }
                            )
                        }

                        items(categories, key = { it.id }) { category ->
                            val count = items.count { it.belongsToCategory(category.id) }
                            FilterChip(
                                selected = selectedCategoryFilter == category.id,
                                onClick = { selectedCategoryFilter = category.id },
                                label = { Text("${category.name} ($count)") },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(category.color)))
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Lista zakupów
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            if (showOnlyUnpurchased) "Brak zakupów do kupienia" else "Brak zakupów",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            category = item.resolvedCategoryIds().mapNotNull { id ->
                                categories.find { it.id == id }
                            },
                            selectedForCalc = item.id in calcSelectedIds,
                            onToggleCalcSelected = {
                                calcSelectedIds = if (item.id in calcSelectedIds) {
                                    calcSelectedIds - item.id
                                } else {
                                    calcSelectedIds + item.id
                                }
                                calcMode = "selected"
                            },
                            onClick = { selectedItem = item },
                            onTogglePurchased = { shoppingViewModel.togglePurchased(item.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialogi
    if (showAddItemDialog) {
        AddShoppingItemDialog(
            categories = categories,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { item ->
                shoppingViewModel.addItem(item)
                showAddItemDialog = false
            }
        )
    }

    if (showAddCategoryDialog) {
        ManageShoppingCategoriesDialog(
            categories = categories,
            onDismiss = { showAddCategoryDialog = false },
            onAddCategory = { category ->
                shoppingViewModel.addCategory(category)
            },
            onDeleteCategory = { categoryId ->
                shoppingViewModel.deleteCategory(categoryId)
            }
        )
    }

    selectedItem?.let { item ->
        EditShoppingItemDialog(
            item = item,
            categories = categories,
            onDismiss = { selectedItem = null },
            onSave = { updatedItem ->
                shoppingViewModel.updateItem(updatedItem)
                selectedItem = null
            },
            onDelete = {
                shoppingViewModel.deleteItem(item.id)
                selectedItem = null
            }
        )
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    category: List<ShoppingCategory>,
    selectedForCalc: Boolean = false,
    onToggleCalcSelected: () -> Unit = {},
    onClick: () -> Unit,
    onTogglePurchased: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = { onTogglePurchased() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                    ),
                    color = if (item.isPurchased)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format("%.2f", item.price)} zl",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isPurchased)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    )

                    category.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(android.graphics.Color.parseColor(cat.color)).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(android.graphics.Color.parseColor(cat.color)),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (item.isPurchased && item.purchasedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kupiono: ${item.purchasedAt.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            IconButton(onClick = onToggleCalcSelected) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = "Do kalkulatora",
                    tint = if (selectedForCalc)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            }
        }
    }
}

@Composable
fun AddShoppingItemDialog(
    categories: List<ShoppingCategory>,
    onDismiss: () -> Unit,
    onConfirm: (ShoppingItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy zakup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa produktu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            price = it
                        }
                    },
                    label = { Text("Cena (zł)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("zł") }
                )

                if (categories.isNotEmpty()) {
                    Text("Kategorie (można wiele):", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories, key = { it.id }) { category ->
                            val selected = category.id in selectedCategoryIds
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedCategoryIds = if (selected) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                },
                                label = { Text(category.name) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(category.color)))
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val ids = selectedCategoryIds.toList()
                        onConfirm(
                            ShoppingItem(
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                categoryId = ids.firstOrNull(),
                                categoryIds = ids
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun EditShoppingItemDialog(
    item: ShoppingItem,
    categories: List<ShoppingCategory>,
    onDismiss: () -> Unit,
    onSave: (ShoppingItem) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price.toString()) }
    var selectedCategoryIds by remember { mutableStateOf(item.resolvedCategoryIds().toSet()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj zakup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa produktu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            price = it
                        }
                    },
                    label = { Text("Cena (zł)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { Text("zł") }
                )

                if (categories.isNotEmpty()) {
                    Text("Kategorie (można wiele):", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories, key = { it.id }) { category ->
                            val selected = category.id in selectedCategoryIds
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedCategoryIds = if (selected) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                },
                                label = { Text(category.name) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(category.color)))
                                    )
                                }
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Usuń zakup")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val ids = selectedCategoryIds.toList()
                        onSave(
                            item.copy(
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                categoryId = ids.firstOrNull(),
                                categoryIds = ids
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usunąć zakup?") },
            text = { Text("Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
fun ManageShoppingCategoriesDialog(
    categories: List<ShoppingCategory>,
    onDismiss: () -> Unit,
    onAddCategory: (ShoppingCategory) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }

    val availableColors = listOf(
        "#4CAF50", "#FF6B35", "#2196F3", "#FFD700",
        "#9C27B0", "#FF5722", "#00BCD4", "#FFC107"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zarządzaj kategoriami") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dodaj nową kategorię
                Text("Dodaj kategorię:", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Nazwa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.padding(4.dp)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategory(ShoppingCategory(name = newCategoryName, color = selectedColor))
                            newCategoryName = ""
                        }
                    },
                    enabled = newCategoryName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj")
                }

                Divider()

                // Lista istniejących kategorii
                Text("Istniejące kategorie:", style = MaterialTheme.typography.labelMedium)

                categories.forEach { category ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(category.color)))
                                )
                                Text(category.name, style = MaterialTheme.typography.bodyMedium)
                            }

                            IconButton(
                                onClick = { onDeleteCategory(category.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Usuń",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}