/*
 * SponsorFlow Nexus v2.3 - Offline Queue DAO
 */
package com.sponsorflow.nexus.offline

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineQueueDao {
    
    @Query("SELECT * FROM offline_queue ORDER BY priority ASC, timestamp ASC")
    fun getAllPending(): Flow<List<OfflineQueueEntity>>
    
    @Query("SELECT * FROM offline_queue ORDER BY priority ASC, timestamp ASC")
    suspend fun getAllPendingList(): List<OfflineQueueEntity>
    
    @Query("SELECT * FROM offline_queue WHERE type = :type ORDER BY timestamp ASC")
    suspend fun getByType(type: String): List<OfflineQueueEntity>
    
    @Query("SELECT COUNT(*) FROM offline_queue")
    fun getCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM offline_queue")
    suspend fun getCountSync(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OfflineQueueEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OfflineQueueEntity>)
    
    @Update
    suspend fun update(item: OfflineQueueEntity)
    
    @Delete
    suspend fun delete(item: OfflineQueueEntity)
    
    @Query("DELETE FROM offline_queue WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM offline_queue")
    suspend fun deleteAll()
    
    @Query("DELETE FROM offline_queue WHERE type = :type")
    suspend fun deleteByType(type: String)
    
    @Query("SELECT * FROM offline_queue WHERE attempts >= :maxAttempts")
    suspend fun getFailedItems(maxAttempts: Int = 5): List<OfflineQueueEntity>
    
    @Query("UPDATE offline_queue SET attempts = attempts + 1, lastAttempt = :timestamp, lastError = :error WHERE id = :id")
    suspend fun incrementAttempts(id: Long, timestamp: Long, error: String?)
    
    @Transaction
    suspend fun insertAndReturn(item: OfflineQueueEntity): OfflineQueueEntity {
        val id = insert(item)
        return item.copy(id = id)
    }
}