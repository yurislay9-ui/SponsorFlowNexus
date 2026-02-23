/*
 * SponsorFlow Nexus v2.4 - Product Repository
 */
package com.sponsorflow.nexus.data.repositories

import com.sponsorflow.nexus.core.contracts.repository.IRepository
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.dao.ProductDao
import com.sponsorflow.nexus.data.entity.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(private val dao: ProductDao) : IRepository<ProductEntity, Long> {
    
    override suspend fun insert(entity: ProductEntity): AppResult<Long> = withContext(Dispatchers.IO) {
        try {
            AppResult.Success(dao.insert(entity))
        } catch (e: Exception) {
            AppResult.Error(AppError.DatabaseError(e))
        }
    }

    override suspend fun update(entity: ProductEntity): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.update(entity)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.DatabaseError(e))
        }
    }

    override suspend fun delete(id: Long): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.deleteById(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.DatabaseError(e))
        }
    }

    override suspend fun getById(id: Long): AppResult<ProductEntity?> = withContext(Dispatchers.IO) {
        try {
            AppResult.Success(dao.getById(id))
        } catch (e: Exception) {
            AppResult.Error(AppError.DatabaseError(e))
        }
    }

    override suspend fun getAll(): AppResult<List<ProductEntity>> = withContext(Dispatchers.IO) {
        try {
            AppResult.Success(dao.getAll())
        } catch (e: Exception) {
            AppResult.Error(AppError.DatabaseError(e))
        }
    }

    suspend fun getByCategory(category: String): AppResult<List<ProductEntity>> = withContext(Dispatchers.IO) {
        try {
            AppResult.Success(dao.getByCategory(category))
        } catch (e: Exception) {
            AppResult.Error(AppError.DatabaseError(e))
        }
    }
}
