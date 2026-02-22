/*
 * SponsorFlow Nexus - Inventory ViewModel
 */
package com.sponsorflow.nexus.ui.inventory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.data.entity.StockStatus

class InventoryViewModel : ViewModel() {
    private val _products = mutableStateListOf<ProductEntity>()
    val products: List<ProductEntity> get() = _products.toList()
    
    var searchQuery by mutableStateOf("")
        private set
    
    var showAddDialog by mutableStateOf(false)
        private set
    
    init { loadProducts() }
    
    private fun loadProducts() {
        _products.addAll(listOf(
            ProductEntity(1, "SKU-001", "Camiseta Premium", "Algodón 100%", 29.99, 15.0, 50, 10, "Ropa"),
            ProductEntity(2, "SKU-002", "Zapatillas Sport", "Running", 89.99, 45.0, 3, 5, "Calzado"),
            ProductEntity(3, "SKU-003", "Mochila Pro", "20L resistente", 45.0, 20.0, 0, 5, "Accesorios"),
            ProductEntity(4, "SKU-004", "Gorra Classic", "Ajustable", 19.99, 8.0, 25, 10, "Accesorios")
        ))
    }
    
    fun onSearchChange(query: String) { searchQuery = query }
    
    fun getFilteredProducts(): List<ProductEntity> {
        if (searchQuery.isBlank()) return products
        return products.filter { it.name.contains(searchQuery, true) || it.sku.contains(searchQuery, true) }
    }
    
    fun increaseStock(productId: Long) {
        val index = _products.indexOfFirst { it.id == productId }
        if (index >= 0) { _products[index] = _products[index].copy(stockQuantity = _products[index].stockQuantity + 1) }
    }
    
    fun decreaseStock(productId: Long) {
        val index = _products.indexOfFirst { it.id == productId }
        if (index >= 0 && _products[index].stockQuantity > 0) {
            _products[index] = _products[index].copy(stockQuantity = _products[index].stockQuantity - 1)
        }
    }
    
    fun addProduct(product: ProductEntity) { _products.add(product); showAddDialog = false }
    fun openAddDialog() { showAddDialog = true }
    fun closeAddDialog() { showAddDialog = false }
    
    fun getStats(): InventoryStats = InventoryStats(
        totalProducts = products.size,
        inStock = products.count { it.getStockStatus() == StockStatus.IN_STOCK },
        lowStock = products.count { it.getStockStatus() == StockStatus.LOW_STOCK },
        outOfStock = products.count { it.getStockStatus() == StockStatus.OUT_OF_STOCK },
        totalValue = products.sumOf { it.price * it.stockQuantity }
    )
}

data class InventoryStats(
    val totalProducts: Int,
    val inStock: Int,
    val lowStock: Int,
    val outOfStock: Int,
    val totalValue: Double
)