/*
 * SponsorFlow Nexus v1.0 - Product Entity
 * Entidad robusta para e-commerce profesional
 */
package com.sponsorflow.nexus.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["category"]),
        Index(value = ["isActive"]),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Identificación
    val sku: String = "",                    // Código de inventario
    val name: String,
    val description: String = "",
    
    // Precios (BigDecimal para precisión)
    val price: Double,                       // Precio de venta
    val costPrice: Double = 0.0,             // Precio de costo (ganancias)
    
    // Inventario
    val stockQuantity: Int = 0,              // Stock actual
    val minStockAlert: Int = 5,              // umbral de alerta
    
    // Categorización
    val category: String = "general",
    val imageUrl: String? = null,
    
    // Estado
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Estados de stock para UI
    fun getStockStatus(): StockStatus {
        return when {
            stockQuantity <= 0 -> StockStatus.OUT_OF_STOCK
            stockQuantity <= minStockAlert -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }
    }
    
    fun isAvailable(): Boolean = isActive && stockQuantity > 0
    
    fun calculateProfit(): Double = price - costPrice
    
    fun calculateProfitMargin(): Double {
        if (costPrice == 0.0) return 0.0
        return ((price - costPrice) / costPrice) * 100
    }
}

enum class StockStatus {
    IN_STOCK,      // Verde
    LOW_STOCK,     // Amarillo
    OUT_OF_STOCK   // Rojo
}
