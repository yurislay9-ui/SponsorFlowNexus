/**
 * SponsorFlow Nexus v1.0 - Inventory Management
 * 
 * ViewModel para la gestión de inventario que implementa el patrón ViewModel
 * de Android Architecture Components. Proporciona una capa de abstracción
 * entre la UI y la lógica de negocio del inventario.
 * 
 * Este componente gestiona el estado de la UI relacionado con el inventario
 * y coordina las operaciones de lectura/escritura en la base de datos.
 * 
 * @author SponsorFlow Nexus Team
 * @version 1.0
 * @since 1.0
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
/**
 * ViewModel para la gestión de inventario con inyección de dependencias.
 * 
 * Este ViewModel implementa el patrón ViewModel de Android Architecture Components
 * y utiliza Hilt para la inyección de dependencias. Gestiona el estado de la UI
 * relacionado con el inventario y coordina las operaciones de base de datos.
 * 
 * @property productDao DAO para operaciones de base de datos de productos
 * 
 * @see ViewModel
 * @see HiltViewModel
 * @see ProductDao
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productDao: ProductDao
) : ViewModel() {
    
    /**
     * Flow mutable para la lista de productos.
     * 
     * Contiene la lista actual de productos y notifica a los observadores
     * cuando cambia el estado.
     */
    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    
    /**
     * Flow inmutable para la lista de productos.
     * 
     * Expone la lista de productos de forma segura para la UI.
     */
    val products: StateFlow<List<ProductEntity>> = _products.asStateFlow()
    
    /**
     * Flow mutable para la consulta de búsqueda.
     * 
     * Contiene el texto actual de búsqueda ingresado por el usuario.
     */
    private val _searchQuery = MutableStateFlow("")
    
    /**
     * Flow inmutable para la consulta de búsqueda.
     * 
     * Expone el texto de búsqueda de forma segura para la UI.
     */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    /**
     * Flow mutable para el estado del diálogo de agregar producto.
     * 
     * Controla si el diálogo de agregar producto está visible o no.
     */
    private val _showAddDialog = MutableStateFlow(false)
    
    /**
     * Flow inmutable para el estado del diálogo de agregar producto.
     * 
     * Expone el estado del diálogo de forma segura para la UI.
     */
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    
    /**
     * Inicializa el ViewModel cargando los productos desde la base de datos.
     */
    init { 
        loadProducts() 
    }
    
    /**
     * Carga los productos desde la base de datos.
     * 
     * Este método utiliza el viewModelScope para lanzar una coroutines
     * que se suscribe al Flow de productos del DAO. Cuando los datos
     * cambian en la base de datos, el Flow emite nuevos valores y se
     * actualiza el estado del ViewModel.
     * 
     * @see productDao
     * @see _products
     */
    private fun loadProducts() {
        viewModelScope.launch {
            try {
                productDao.getAll().collect { productList ->
                    _products.value = productList
                }
            } catch (e: Exception) {
                // En caso de error, se establece una lista vacía
                _products.value = emptyList()
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
    
    /**
     * Método de limpieza del ViewModel.
     * 
     * Este método se llama cuando el ViewModel es desechado y el scope
     * de la ViewModel es cancelado. Aquí se deben limpiar recursos como
     * suscripciones a Flows, coroutines activas, o cualquier otro recurso
     * que pueda causar memory leaks.
     * 
     * En este caso, el viewModelScope se cancela automáticamente, pero
     * se incluye este método para futuras implementaciones que puedan
     * requerir limpieza manual de recursos.
     * 
     * @see ViewModel.onCleared
     * @see viewModelScope
     */
    override fun onCleared() {
        super.onCleared()
        // Limpiar recursos si es necesario
        // El viewModelScope se cancela automáticamente al destruirse el ViewModel
    }
}

data class InventoryStats(
    val totalProducts: Int,
    val inStock: Int,
    val lowStock: Int,
    val outOfStock: Int,
    val totalValue: Double
)