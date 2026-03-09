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

    @Query("DELETE FROM contacts WHERE phone = :phone")
    suspend fun deleteByPhone(phone: String)

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE isBlocked = 0 ORDER BY lastMessageTime DESC")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE isBlocked = 0 ORDER BY lastMessageTime DESC")
    fun getAllFlow(): Flow<List<ContactEntity>>

    @Query("SELECT COUNT(*) FROM contacts WHERE isBlocked = 0")
    suspend fun getCount(): Int
    
    @Query("SELECT COUNT(*) FROM contacts WHERE isBlocked = 0")
    suspend fun getTotalContacts(): Int

    @Query("SELECT * FROM contacts WHERE isBlocked = 1")
    suspend fun getBlockedContacts(): List<ContactEntity>

    @Query("UPDATE contacts SET isBlocked = :blocked WHERE phone = :phone")
    suspend fun setBlocked(phone: String, blocked: Boolean)

    @Query("UPDATE contacts SET messageCount = messageCount + 1 WHERE phone = :phone")
    suspend fun incrementMessageCount(phone: String)

    @Query("SELECT * FROM contacts WHERE lastMessageTime BETWEEN :startTime AND :endTime")
    suspend fun getContactsByTimeRange(startTime: Long, endTime: Long): List<ContactEntity>

    @Query("UPDATE contacts SET lastMessageTime = :timestamp, messageCount = messageCount + 1 WHERE id = :id")
    suspend fun updateLastMessage(id: Long, timestamp: Long)
}