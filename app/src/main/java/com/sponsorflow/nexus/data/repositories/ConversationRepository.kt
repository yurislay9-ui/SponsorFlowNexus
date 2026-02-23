/*
 * SponsorFlow Nexus v1.0 - Conversation Repository
 */
package com.sponsorflow.nexus.data.repositories

import com.sponsorflow.nexus.core.contracts.repository.IRepository
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.dao.ConversationDao
import com.sponsorflow.nexus.data.entity.ConversationEntity

class ConversationRepository(private val dao: ConversationDao) : IRepository<ConversationEntity, Long> {
    override suspend fun insert(entity: ConversationEntity): AppResult<Long> = try {
        AppResult.Success(dao.insert(entity))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun update(entity: ConversationEntity): AppResult<Unit> = try {
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

    override suspend fun getById(id: Long): AppResult<ConversationEntity?> = try {
        AppResult.Success(null)
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    override suspend fun getAll(): AppResult<List<ConversationEntity>> = try {
        AppResult.Success(emptyList())
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    suspend fun getByContact(contactId: Long): AppResult<List<ConversationEntity>> = try {
        AppResult.Success(dao.getByContact(contactId))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    suspend fun getRecent(contactId: Long, limit: Int): AppResult<List<ConversationEntity>> = try {
        AppResult.Success(dao.getRecent(contactId, limit))
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }
}