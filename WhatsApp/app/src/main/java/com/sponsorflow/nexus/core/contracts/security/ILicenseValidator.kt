/*
 * SponsorFlow Nexus v1.0 - License Validator Interface
 * Skill: Seguridad - Validación de licencias
 */
package com.sponsorflow.nexus.core.contracts.security

import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.core.result.AppResult

interface ILicenseValidator {
    suspend fun validate(licenseKey: String): AppResult<LicenseInfo>
    suspend fun refresh(): AppResult<LicenseInfo>
    fun isGracePeriodActive(): Boolean
    fun getRemainingGraceDays(): Int
    fun getCachedLicenseInfo(): LicenseInfo?
    suspend fun clearCache()
}

data class LicenseInfo(
    val key: String,
    val tier: SubscriptionTier,
    val expiresAt: Long,
    val isActive: Boolean,
    val userId: String? = null
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
    fun getDaysRemaining(): Int = maxOf(0, ((expiresAt - System.currentTimeMillis()) / 86400000).toInt())
    fun hasTierOrHigher(required: SubscriptionTier): Boolean = tier.isAtLeast(required)
}