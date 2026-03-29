package com.sponsorflow.nexus.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.inventory.ProductManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryStats(
    val totalProducts: Int = 0,
    val inStock: Int = 0,
    val lowStock: Int = 0,
    val outOfStock: Int = 0
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productManager: ProductManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productManager.getAllProducts().let { result ->
                when (result) {
                    is AppResult.Success -> _products.value = result.data
                    is AppResult.Error -> _products.value = emptyList()
                    is AppResult.Loading -> {}
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
            productManager.addProduct(product).let { result ->
                when (result) {
                    is AppResult.Success -> {
                        closeAddDialog()
                        loadProducts()
                    }
                    is AppResult.Error -> {
                        // Handle error
                    }
                    is AppResult.Loading -> {}
                }
            }
        }
    }

    fun increaseStock(productId: Long) {
        viewModelScope.launch {
            productManager.increaseStock(productId, 1).let { result ->
                when (result) {
                    is AppResult.Success -> loadProducts()
                    is AppResult.Error -> { /* Handle error */ }
                    is AppResult.Loading -> {}
                }
            }
        }
    }

    fun decreaseStock(productId: Long) {
        viewModelScope.launch {
            productManager.decreaseStock(productId, 1).let { result ->
                when (result) {
                    is AppResult.Success -> loadProducts()
                    is AppResult.Error -> { /* Handle error */ }
                    is AppResult.Loading -> {}
                }
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            productManager.deleteProduct(productId).let { result ->
                when (result) {
                    is AppResult.Success -> loadProducts()
                    is AppResult.Error -> { /* Handle error */ }
                    is AppResult.Loading -> {}
                }
            }
        }
    }
}
