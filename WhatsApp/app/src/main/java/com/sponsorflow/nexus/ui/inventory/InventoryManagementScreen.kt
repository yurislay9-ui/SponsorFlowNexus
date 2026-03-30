/*
 * SponsorFlow Nexus - Inventory Management Screen
 * Pantalla completa de gestión de inventario con Compose
 */
package com.sponsorflow.nexus.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.data.entity.StockStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryManagementScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val stats = viewModel.getStats()
    val filteredProducts = viewModel.getFilteredProducts()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir producto"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InventoryStatCard(
                    label = "Total",
                    value = stats.totalProducts.toString(),
                    color = Color.Blue
                )
                InventoryStatCard(
                    label = "En Stock",
                    value = stats.inStock.toString(),
                    color = Color(0xFF4CAF50)
                )
                InventoryStatCard(
                    label = "Bajo",
                    value = stats.lowStock.toString(),
                    color = Color(0xFFFFC107)
                )
                InventoryStatCard(
                    label = "Agotado",
                    value = stats.outOfStock.toString(),
                    color = Color(0xFFF44336)
                )
            }

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query -> viewModel.onSearchChange(query) },
                label = { Text("Buscar producto...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Loading Indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Product List
            if (filteredProducts.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No se encontraron productos" else "No hay productos en el inventario",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredProducts,
                        key = { product -> product.id }
                    ) { product ->
                        InventoryProductCard(
                            product = product,
                            onIncrease = { viewModel.increaseStock(product.id) },
                            onDecrease = { viewModel.decreaseStock(product.id) },
                            onDelete = { viewModel.deleteProduct(product.id) }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Product Dialog
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onAdd = { product -> viewModel.addProduct(product) }
        )
    }
}

@Composable
private fun InventoryStatCard(
    label: String,
    value: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun InventoryProductCard(
    product: ProductEntity,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (product.getStockStatus()) {
        StockStatus.IN_STOCK -> Color(0xFF4CAF50)
        StockStatus.LOW_STOCK -> Color(0xFFFFC107)
        StockStatus.OUT_OF_STOCK -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, RoundedCornerShape(50))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Product info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "SKU: ${product.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.category.isNotEmpty()) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Stock controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDecrease,
                    enabled = product.stockQuantity > 0
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Reducir stock"
                    )
                }

                Text(
                    text = "${product.stockQuantity}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = onIncrease) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar stock"
                    )
                }
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("0") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("5") }

    val isValid = name.isNotBlank() && price.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Nuevo Producto")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock inicial") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Stock mínimo (alerta)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            ProductEntity(
                                id = 0,
                                name = name.trim(),
                                sku = sku.ifBlank { "SKU-${System.currentTimeMillis()}" },
                                price = price.toDoubleOrNull() ?: 0.0,
                                stockQuantity = stock.toIntOrNull() ?: 0,
                                category = category.trim(),
                                description = description.trim(),
                                minStockAlert = minStock.toIntOrNull() ?: 5,
                                imageUrl = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                },
                enabled = isValid
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
