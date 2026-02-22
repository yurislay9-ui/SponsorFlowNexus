/*
 * SponsorFlow Nexus v2.3 - Subscription DAO
 */
package com.sponsorflow.nexus.data.dao

import androidx.room.*
import com.sponsorflow.nexus.data.entity.SubscriptionEntity

@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity)

    @Query("SELECT * FROM subscriptions WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: String): SubscriptionEntity?

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Query("UPDATE subscriptions SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)
}