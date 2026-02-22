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

    suspend fun updateStock(productId: Long, newStock: Int): AppResult<Unit> {
        return productRepo.getById(productId).flatMap { product ->
            if (product != null) {
                val updated = product.copy(stockQuantity = newStock)
                productRepo.update(updated)
            } else {
                com.sponsorflow.nexus.core.result.AppResult.Error(
                    com.sponsorflow.nexus.core.result.AppError.ValidationError("id", "Producto no encontrado")
                )
            }
        }
    }

    suspend fun getCategories(): AppResult<List<String>> {
        return productRepo.getAll().map { products ->
            products.map { it.category }.distinct()
        }
    }
}