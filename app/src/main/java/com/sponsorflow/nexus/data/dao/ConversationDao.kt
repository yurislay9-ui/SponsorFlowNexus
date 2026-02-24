/*
 * SponsorFlow Nexus v1.0 - Conversation DAO
 */
package com.sponsorflow.nexus.data.dao

import androidx.room.*
import com.sponsorflow.nexus.data.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert
    suspend fun insert(message: ConversationEntity): Long

    // CORREGIDO: Agregar métodos faltantes requeridos por ConversationRepository
    @Update
    suspend fun update(message: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    suspend fun getAll(): List<ConversationEntity>

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM conversations WHERE contactId = :contactId ORDER BY timestamp DESC")
    suspend fun getByContact(contactId: Long): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE contactId = :contactId ORDER BY timestamp DESC")
    fun getByContactFlow(contactId: Long): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE contactId = :contactId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(contactId: Long, limit: Int): List<ConversationEntity>

    @Query("DELETE FROM conversations WHERE contactId = :contactId")
    suspend fun deleteByContact(contactId: Long)

    @Query("SELECT COUNT(*) FROM conversations WHERE contactId = :contactId")
    suspend fun getCountByContact(contactId: Long): Int

    @Query("SELECT * FROM conversations WHERE contactId = :contactId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(contactId: Long): ConversationEntity?
}