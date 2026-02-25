/*
 * SponsorFlow Nexus v1.0 - Contact DAO
 */
package com.sponsorflow.nexus.data.dao

import androidx.room.*
import com.sponsorflow.nexus.data.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE isActive = 1 ORDER BY lastMessageAt DESC")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE isActive = 1 ORDER BY lastMessageAt DESC")
    fun getAllFlow(): Flow<List<ContactEntity>>

    @Query("SELECT COUNT(*) FROM contacts WHERE isActive = 1")
    suspend fun getCount(): Int

    @Query("UPDATE contacts SET lastMessageAt = :timestamp, conversationCount = conversationCount + 1 WHERE id = :id")
    suspend fun updateLastMessage(id: Long, timestamp: Long)
}