/*
 * SponsorFlow Nexus v2.3 - Metric DAO
 */
package com.sponsorflow.nexus.data.dao

import androidx.room.*
import com.sponsorflow.nexus.data.entity.MetricEntity

@Dao
interface MetricDao {
    @Insert
    suspend fun insert(metric: MetricEntity): Long

    @Query("SELECT * FROM metrics WHERE eventType = :type ORDER BY timestamp DESC")
    suspend fun getByType(type: String): List<MetricEntity>

    @Query("SELECT * FROM metrics WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getSince(since: Long): List<MetricEntity>

    @Query("DELETE FROM metrics WHERE timestamp < :before")
    suspend fun deleteBefore(before: Long): Int

    @Query("SELECT COUNT(*) FROM metrics WHERE eventType = :type AND timestamp >= :since")
    suspend fun getCountSince(type: String, since: Long): Int
}