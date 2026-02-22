/*
 * SponsorFlow Nexus v2.3 - Product Repository
 */
package com.sponsorflow.nexus.data.repositories

import com.sponsorflow.nexus.core.contracts.repository.IRepository
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.dao.ProductDao
import com.sponsorflow.nexus.data.entity.ProductEntity

class ProductRepository(private val dao: ProductDao) : IRepository<ProductEntity, Long> {
    override suspend fun insert(entity: ProductEntity): AppResult<Long> = try {
        AppResult.Success(dao.insert(entity))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun update(entity: ProductEntity): AppResult<Unit> = try {
        dao.update(entity)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun delete(id: Long): AppResult<Unit> = try {
        dao.deleteById(id)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun getById(id: Long): AppResult<ProductEntity?> = try {
        AppResult.Success(dao.getById(id))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun getAll(): AppResult<List<ProductEntity>> = try {
        AppResult.Success(dao.getAll())
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    suspend fun getByCategory(category: String): AppResult<List<ProductEntity>> = try {
        AppResult.Success(dao.getByCategory(category))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }
}