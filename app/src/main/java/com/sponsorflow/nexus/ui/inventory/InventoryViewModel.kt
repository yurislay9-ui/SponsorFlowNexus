/*
 * SponsorFlow Nexus - Inventory ViewModel
 * CORREGIDO: Persistencia con ProductDao y Flow, Hilt injection
 */
package com.sponsorflow.nexus.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sponsorflow.nexus.data.dao.ProductDao
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.data.entity.StockStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// CORREGIDO: Usar @HiltViewModel para inyección de dependencias
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productDao: ProductDao
) : ViewModel() {
    
    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val products: StateFlow<List<ProductEntity>> = _products.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    
    init { 
        loadProducts() 
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            productDao.getAll().collect { productList ->
                _products.value = productList
            }
        }
    }
    
    fun onSearchChange(query: String) { _searchQuery.value = query }
    
    fun getFilteredProducts(): List<ProductEntity> {
        val query = _searchQuery.value
        val productList = _products.value
        if (query.isBlank()) return productList
        return productList.filter { it.name.contains(query, true) || it.sku.contains(query, true) }
    }
    
    fun increaseStock(productId: Long) {
        viewModelScope.launch {
            val currentList = _products.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == productId }
            if (index >= 0) { 
                val updated = currentList[index].copy(stockQuantity = currentList[index].stockQuantity + 1)
                currentList[index] = updated
                _products.value = currentList
                productDao.update(updated)
            }
        }
    }
    
    fun decreaseStock(productId: Long) {
        viewModelScope.launch {
            val currentList = _products.value.toMutableList()
            val index = currentList.indexOfFirst { it.id == productId }
            if (index >= 0 && currentList[index].stockQuantity > 0) {
                val updated = currentList[index].copy(stockQuantity = currentList[index].stockQuantity - 1)
                currentList[index] = updated
                _products.value = currentList
                productDao.update(updated)
            }
        }
    }
    
    fun addProduct(product: ProductEntity) { 
        viewModelScope.launch {
            productDao.insert(product)
            _showAddDialog.value = false
        }
    }
    fun openAddDialog() { _showAddDialog.value = true }
    fun closeAddDialog() { _showAddDialog.value = false }
    
    fun getStats(): InventoryStats {
        val productList = _products.value
        return InventoryStats(
            totalProducts = productList.size,
            inStock = productList.count { it.getStockStatus() == StockStatus.IN_STOCK },
            lowStock = productList.count { it.getStockStatus() == StockStatus.LOW_STOCK },
            outOfStock = productList.count { it.getStockStatus() == StockStatus.OUT_OF_STOCK },
            totalValue = productList.sumOf { it.price * it.stockQuantity }
        )
    }
}

data class InventoryStats(
    val totalProducts: Int,
    val inStock: Int,
    val lowStock: Int,
    val outOfStock: Int,
    val totalValue: Double
)