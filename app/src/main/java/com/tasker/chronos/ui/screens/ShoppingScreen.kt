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

    // Filtrowane przedmioty
    val filteredItems = remember(items, selectedCategoryFilter, showOnlyUnpurchased) {
        items.filter { item ->
            val categoryMatch = selectedCategoryFilter == null || item.categoryId == selectedCategoryFilter
            val purchasedMatch = !showOnlyUnpurchased || !item.isPurchased
            categoryMatch && purchasedMatch
        }.sortedWith(
            compareBy<ShoppingItem> { it.isPurchased }
                .thenByDescending { it.createdAt }
        )
    }

    val totalPrice = shoppingViewModel.getTotalPrice()
    val purchasedTotal = shoppingViewModel.getPurchasedTotal()

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
            // Statystyki
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Do kupienia:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${String.format("%.2f", totalPrice)} zł",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                            val count = items.count { it.categoryId == category.id }
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
                            category = categories.find { it.id == item.categoryId },
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
    category: ShoppingCategory?,
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
                        text = "${String.format("%.2f", item.price)} zł",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isPurchased)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    )

                    if (category != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(android.graphics.Color.parseColor(category.color)),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (item.isPurchased && item.purchasedAt != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Kupiono: ${item.purchasedAt.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50)
                    )
                }
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
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

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
                        // Pozwól tylko na liczby i jedną kropkę
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
                    Text("Kategoria:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("Brak") }
                            )
                        }
                        items(categories, key = { it.id }) { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id },
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
                        onConfirm(
                            ShoppingItem(
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                categoryId = selectedCategoryId
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
    var selectedCategoryId by remember { mutableStateOf(item.categoryId) }
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
                    Text("Kategoria:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("Brak") }
                            )
                        }
                        items(categories, key = { it.id }) { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { selectedCategoryId = category.id },
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
                        onSave(
                            item.copy(
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                categoryId = selectedCategoryId
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
            title = { Text("Usuń zakup?") },
            text = { Text("Czy na pewno chcesz usunąć ten zakup? Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Anuluj")
                }
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