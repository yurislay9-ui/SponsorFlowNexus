/*
 * SponsorFlow Nexus v2.3 - Subscription Repository
 */
package com.sponsorflow.nexus.data.repositories

import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import com.sponsorflow.nexus.data.dao.SubscriptionDao
import com.sponsorflow.nexus.data.entity.SubscriptionEntity

class SubscriptionRepository(private val dao: SubscriptionDao) {

    suspend fun getActive(): AppResult<SubscriptionEntity?> = try {
        AppResult.Success(dao.getActive())
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    suspend fun activate(id: String, tier: SubscriptionTier, txHash: String?, durationDays: Int = 30): AppResult<Unit> = try {
        val now = System.currentTimeMillis()
        val entity = SubscriptionEntity(
            id = id,
            tier = tier.name,
            txHash = txHash,
            startDate = now,
            endDate = now + (durationDays * 86400000L),
            gracePeriodEnd = now + ((durationDays + 3) * 86400000L),
            isActive = true
        )
        dao.insert(entity)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }

    suspend fun deactivate(id: String): AppResult<Unit> = try {
        dao.deactivate(id)
        AppResult.Success(Unit)
    } catch (e: Exception) {
        AppResult.Error(AppError.DatabaseError(e))
    }
}