/*
 * SponsorFlow Nexus v1.0 - Subscription Entity
 */
package com.sponsorflow.nexus.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subscriptions",
    indices = [Index(value = ["isActive"]), Index(value = ["endDate"])]
)
data class SubscriptionEntity(
    @PrimaryKey
    val id: String,
    val tier: String,
    val txHash: String? = null,
    val startDate: Long,
    val endDate: Long,
    val gracePeriodEnd: Long? = null,
    val isActive: Boolean = true
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > endDate

    fun isInGracePeriod(): Boolean {
        val graceEnd = gracePeriodEnd ?: return false
        return isExpired() && System.currentTimeMillis() <= graceEnd
    }

    fun getDaysRemaining(): Int {
        val endTime = if (isExpired() && gracePeriodEnd != null) gracePeriodEnd else endDate
        val remaining = endTime - System.currentTimeMillis()
        return if (remaining > 0) (remaining / 86400000).toInt() else 0
    }
}