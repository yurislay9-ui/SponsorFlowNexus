package com.sponsorflow.nexus.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.inventory.ProductManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryStats(
    val totalProducts: Int = 0,
    val inStock: Int = 0,
    val lowStock: Int = 0,
    val outOfStock: Int = 0
)

@Composable
fun rememberInventoryViewModel(): InventoryViewModel {
    return hiltViewModel()
}

@Composable
fun InventoryManagementScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
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

            // Search
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

    // Add Dialog
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onAdd = { viewModel.addProduct(it) }
        )
    }
}

class InventoryViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var productManager: ProductManager? = null

    fun setProductManager(manager: ProductManager) {
        productManager = manager
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productManager?.getAllProducts()?.let { result ->
                when (result) {
                    is AppResult.Success -> _products.value = result.data
                    is AppResult.Error -> _products.value = emptyList()
                }
            }
            _isLoading.value = false
        }
    }

    fun getStats(): InventoryStats {
        val products = _products.value
        return InventoryStats(
            totalProducts = products.size,
            inStock = products.count { it.stockQuantity > it.minStockAlert },
            lowStock = products.count { it.stockQuantity in 1..it.minStockAlert },
            outOfStock = products.count { it.stockQuantity <= 0 }
        )
    }

    fun getFilteredProducts(): List<ProductEntity> {
        val query = _searchQuery.value.lowercase()
        return if (query.isBlank()) {
            _products.value
        } else {
            _products.value.filter {
                it.name.lowercase().contains(query) ||
                it.sku.lowercase().contains(query) ||
                it.category.lowercase().contains(query)
            }
        }
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            productManager?.addProduct(product)?.let { result ->
                when (result) {
                    is AppResult.Success -> {
                        closeAddDialog()
                        loadProducts()
                    }
                    is AppResult.Error -> {
                        // Handle error
                    }
                }
            }
        }
    }

    fun increaseStock(productId: Long) {
        viewModelScope.launch {
            productManager?.increaseStock(productId, 1)?.let { result ->
                when (result) {
                    is AppResult.Success -> loadProducts()
                    is AppResult.Error -> { /* Handle error */ }
                }
            }
        }
    }

    fun decreaseStock(productId: Long) {
        viewModelScope.launch {
            productManager?.decreaseStock(productId, 1)?.let { result ->
                when (result) {
                    is AppResult.Success -> loadProducts()
                    is AppResult.Error -> { /* Handle error */ }
                }
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            productManager?.deleteProduct(productId)?.let { result ->
                when (result) {
                    is AppResult.Success -> loadProducts()
                    is AppResult.Error -> { /* Handle error */ }
                }
            }
        }
    }
}
