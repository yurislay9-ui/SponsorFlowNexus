/*
 * SponsorFlow Nexus v2.4 - Product DAO
 * Con operaciones atómicas de inventario
 */
package com.sponsorflow.nexus.data.dao

import androidx.room.*
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.data.entity.StockStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    // === INSERTAR / ACTUALIZAR ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    // === CONSULTAS BÁSICAS ===
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku")
    suspend fun getBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getActive(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1")
    suspend fun getByCategory(category: String): List<ProductEntity>

    @Query("SELECT DISTINCT category FROM products WHERE isActive = 1")
    suspend fun getCategories(): List<String>

    // === OPERACIONES DE STOCK ATÓMICAS ===
    
    /** Resta stock atómicamente. Retorna true si exitoso, false si no hay suficiente */
    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity, updatedAt = :timestamp WHERE id = :productId AND stockQuantity >= :quantity")
    suspend fun decreaseStock(productId: Long, quantity: Int, timestamp: Long = System.currentTimeMillis()): Int

    /** Aumenta stock atómicamente */
    @Query("UPDATE products SET stockQuantity = stockQuantity + :quantity, updatedAt = :timestamp WHERE id = :productId")
    suspend fun increaseStock(productId: Long, quantity: Int, timestamp: Long = System.currentTimeMillis()): Int

    /** Verifica si hay stock disponible */
    @Query("SELECT stockQuantity FROM products WHERE id = :productId")
    suspend fun getStock(productId: Long): Int?

    /** Verifica disponibilidad para compra */
    @Query("SELECT COUNT(*) > 0 FROM products WHERE id = :productId AND isActive = 1 AND stockQuantity >= :quantity")
    suspend fun isAvailable(productId: Long, quantity: Int = 1): Boolean

    // === ALERTAS DE STOCK ===
    
    /** Productos con stock bajo (para notificaciones) */
    @Query("SELECT * FROM products WHERE isActive = 1 AND stockQuantity > 0 AND stockQuantity <= minStockAlert")
    suspend fun getLowStockProducts(): List<ProductEntity>

    /** Productos agotados */
    @Query("SELECT * FROM products WHERE isActive = 1 AND stockQuantity <= 0")
    suspend fun getOutOfStockProducts(): List<ProductEntity>

    /** Conteo de productos con problemas de stock */
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND stockQuantity <= minStockAlert")
    suspend fun getAlertCount(): Int

    // === FLOWS PARA UI ===
    
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%'")
    fun searchFlow(query: String): Flow<List<ProductEntity>>

    // === ESTADÍSTICAS ===
    
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun getCount(): Int

    @Query("SELECT SUM(stockQuantity) FROM products WHERE isActive = 1")
    suspend fun getTotalStock(): Int?

    @Query("SELECT SUM(price * stockQuantity) FROM products WHERE isActive = 1")
    suspend fun getInventoryValue(): Double?
}
