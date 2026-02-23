/*
 * SponsorFlow Nexus v2.3 - Product Manager
 */
package com.sponsorflow.nexus.inventory

import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.data.repositories.ProductRepository

class ProductManager(private val productRepo: ProductRepository) {

    suspend fun addProduct(
        name: String,
        description: String,
        price: Double,
        category: String,
        stockQuantity: Int = 0
    ): AppResult<Long> {
        val product = ProductEntity(
            name = name,
            description = description,
            price = price,
            category = category,
            stockQuantity = stockQuantity
        )
        return productRepo.insert(product)
    }

    suspend fun searchProducts(query: String): AppResult<List<ProductEntity>> {
        return productRepo.getAll().map { products ->
            products.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun getProductsByCategory(category: String): AppResult<List<ProductEntity>> {
        return productRepo.getByCategory(category)
    }

    /**
     * Actualiza stock de forma atómica usando operaciones del DAO
     * CORREGIDO: Usa operaciones atómicas en lugar de read-modify-write
     */
    suspend fun updateStock(productId: Long, newStock: Int): AppResult<Unit> {
        val currentStock = productRepo.getStock(productId)
        
        return when {
            currentStock == null -> {
                AppResult.Error(AppError.ValidationError("id", "Producto no encontrado"))
            }
            newStock < 0 -> {
                AppResult.Error(AppError.ValidationError("stock", "Stock no puede ser negativo"))
            }
            newStock > currentStock -> {
                // Aumentar stock - operación atómica
                val increase = newStock - currentStock
                productRepo.increaseStock(productId, increase)
                AppResult.Success(Unit)
            }
            newStock < currentStock -> {
                // Disminuir stock - operación atómica
                val decrease = currentStock - newStock
                val success = productRepo.decreaseStock(productId, decrease)
                if (success) {
                    AppResult.Success(Unit)
                } else {
                    AppResult.Error(AppError.ValidationError("stock", "Stock insuficiente"))
                }
            }
            else -> {
                // Sin cambios
                AppResult.Success(Unit)
            }
        }
    }
    
    /**
     * Aumenta stock de forma atómica
     */
    suspend fun increaseStock(productId: Long, quantity: Int): AppResult<Unit> {
        val success = productRepo.increaseStock(productId, quantity)
        return if (success) {
            AppResult.Success(Unit)
        } else {
            AppResult.Error(AppError.ValidationError("id", "Producto no encontrado"))
        }
    }
    
    /**
     * Disminuye stock de forma atómica
     */
    suspend fun decreaseStock(productId: Long, quantity: Int): AppResult<Unit> {
        val success = productRepo.decreaseStock(productId, quantity)
        return if (success) {
            AppResult.Success(Unit)
        } else {
            AppResult.Error(AppError.ValidationError("stock", "Stock insuficiente"))
        }
    }

    suspend fun getCategories(): AppResult<List<String>> {
        return productRepo.getAll().map { products ->
            products.map { it.category }.distinct()
        }
    }
}