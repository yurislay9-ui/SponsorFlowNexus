/*
 * SponsorFlow Nexus v1.0 - Product Manager
 * CORREGIDO: Thread-safe, operaciones atómicas
 */
package com.sponsorflow.nexus.inventory

import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.data.entity.ProductEntity
import com.sponsorflow.nexus.data.repositories.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductManager @Inject constructor(
    private val productRepo: ProductRepository
) {
    private val stockMutex = Mutex()

    suspend fun getAllProducts(): AppResult<List<ProductEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                productRepo.getAll()
            } catch (e: IllegalArgumentException) {
                AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
            } catch (e: IllegalStateException) {
                AppResult.Error(AppError.DatabaseError(e))
            } catch (e: SecurityException) {
                AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
            } catch (e: Exception) {
                AppResult.Error(e.message?.let { AppError.ParseError(it) } ?: AppError.Unknown)
            }
        }
    }

    suspend fun getProduct(id: Long): AppResult<ProductEntity?> {
        return withContext(Dispatchers.IO) {
            try {
                productRepo.getById(id)
            } catch (e: IllegalArgumentException) {
                AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
            } catch (e: IllegalStateException) {
                AppResult.Error(AppError.DatabaseError(e))
            } catch (e: SecurityException) {
                AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
            } catch (e: Exception) {
                AppResult.Error(e.message?.let { AppError.ParseError(it) } ?: AppError.Unknown)
            }
        }
    }

    // CORREGIDO: Operación atómica con mutex
    suspend fun decreaseStock(productId: Long, quantity: Int): AppResult<Unit> {
        return stockMutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    val productResult = productRepo.getById(productId)
                    when (productResult) {
                        is AppResult.Success -> {
                            val product = productResult.data
                            if (product == null) {
                                AppResult.Error(AppError.NotFound("Producto no encontrado"))
                            } else if (product.stockQuantity < quantity) {
                                AppResult.Error(AppError.InsufficientStock("Stock insuficiente"))
                            } else {
                                val updatedProduct = product.copy(stockQuantity = product.stockQuantity - quantity)
                                productRepo.update(updatedProduct)
                            }
                        }
                        is AppResult.Error -> productResult
                        else -> AppResult.Error(AppError.Unknown)
                    }
                } catch (e: IllegalArgumentException) {
                    AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
                } catch (e: IllegalStateException) {
                    AppResult.Error(AppError.DatabaseError(e))
                } catch (e: SecurityException) {
                    AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
                } catch (e: Exception) {
                    AppResult.Error(e.message?.let { AppError.ParseError(it) } ?: AppError.Unknown)
                }
            }
        }
    }

    suspend fun increaseStock(productId: Long, quantity: Int): AppResult<Unit> {
        return stockMutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    val productResult = productRepo.getById(productId)
                    when (productResult) {
                        is AppResult.Success -> {
                            val product = productResult.data
                            if (product == null) {
                                AppResult.Error(AppError.NotFound("Producto no encontrado"))
                            } else {
                                val updatedProduct = product.copy(stockQuantity = product.stockQuantity + quantity)
                                productRepo.update(updatedProduct)
                            }
                        }
                        is AppResult.Error -> productResult
                        else -> AppResult.Error(AppError.Unknown)
                    }
                } catch (e: IllegalArgumentException) {
                    AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
                } catch (e: IllegalStateException) {
                    AppResult.Error(AppError.DatabaseError(e))
                } catch (e: SecurityException) {
                    AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
                } catch (e: Exception) {
                    AppResult.Error(e.message?.let { AppError.ParseError(it) } ?: AppError.Unknown)
                }
            }
        }
    }

    suspend fun addProduct(product: ProductEntity): AppResult<Long> {
        return withContext(Dispatchers.IO) {
            try {
                productRepo.insert(product)
            } catch (e: IllegalArgumentException) {
                AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
            } catch (e: IllegalStateException) {
                AppResult.Error(AppError.DatabaseError(e))
            } catch (e: SecurityException) {
                AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
            } catch (e: Exception) {
                AppResult.Error(e.message?.let { AppError.ParseError(it) } ?: AppError.Unknown)
            }
        }
    }

    suspend fun deleteProduct(id: Long): AppResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                productRepo.delete(id)
            } catch (e: IllegalArgumentException) {
                AppResult.Error(AppError.ValidationError(e.message ?: "Invalid parameters"))
            } catch (e: IllegalStateException) {
                AppResult.Error(AppError.DatabaseError(e))
            } catch (e: SecurityException) {
                AppResult.Error(AppError.SecurityError(e.message ?: "Security error"))
            } catch (e: Exception) {
                AppResult.Error(e.message?.let { AppError.ParseError(it) } ?: AppError.Unknown)
            }
        }
    }
}
