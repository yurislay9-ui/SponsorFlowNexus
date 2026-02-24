/*
 * SponsorFlow Nexus - Inventory Management Screen
 * CORREGIDO: Usar hiltViewModel() y collectAsState()
 */
package com.sponsorflow.nexus.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryManagementScreen(
    // CORREGIDO: Usar hiltViewModel() para inyección de dependencias
    viewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    // CORREGIDO: Recolectar StateFlows con collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    
    val stats = viewModel.getStats()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, "Añadir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("Total", stats.totalProducts.toString(), Color.Blue)
                StatCard("En Stock", stats.inStock.toString(), Color(0xFF4CAF50))
                StatCard("Bajo", stats.lowStock.toString(), Color(0xFFFFC107))
                StatCard("Agotado", stats.outOfStock.toString(), Color(0xFFF44336))
            }
            
            // Search - CORREGIDO: usar variable local searchQuery
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                label = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            
            // Product List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.getFilteredProducts()) { product ->
                    ProductCard(
                        product = product,
                        onIncrease = { viewModel.increaseStock(product.id) },
                        onDecrease = { viewModel.decreaseStock(product.id) }
                    )
                }
            }
        }
    }
    
    // Add Dialog - CORREGIDO: usar variable local showAddDialog
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onAdd = { viewModel.addProduct(it) }
        )
    }
}