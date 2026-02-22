/*
 * SponsorFlow Nexus v2.3 - Contact Repository
 */
package com.sponsorflow.nexus.data.repositories

import com.sponsorflow.nexus.core.contracts.repository.IRepository
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.dao.ContactDao
import com.sponsorflow.nexus.data.entity.ContactEntity

class ContactRepository(private val dao: ContactDao) : IRepository<ContactEntity, Long> {
    override suspend fun insert(entity: ContactEntity): AppResult<Long> = try {
        AppResult.Success(dao.insert(entity))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun update(entity: ContactEntity): AppResult<Unit> = try {
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

    override suspend fun getById(id: Long): AppResult<ContactEntity?> = try {
        AppResult.Success(dao.getById(id))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun getAll(): AppResult<List<ContactEntity>> = try {
        AppResult.Success(dao.getAll())
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    suspend fun getByPhone(phone: String): AppResult<ContactEntity?> = try {
        AppResult.Success(dao.getByPhone(phone))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }
}