package com.sponsorflow.nexus.ban

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class BanRiskLevel {
    SAFE,
    WARNING,
    DANGER,
    BANNED
}

data class BanStatus(
    val isBanned: Boolean,
    val riskLevel: BanRiskLevel,
    val indicators: List<String> = emptyList(),
    val lastChecked: Long = System.currentTimeMillis(),
    val recommendedAction: String = ""
)

class BanDetectionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()

    private var accountDisabledDetected: Boolean = false
    private var messageBlockedCount: Int = 0
    private var rateLimitHitCount: Int = 0
    private var suspiciousResponseCount: Int = 0
    private var lastCheckTimestamp: Long = 0L

    init {
        loadState()
    }

    private fun loadState() {
        accountDisabledDetected = prefs.getBoolean(KEY_ACCOUNT_DISABLED, false)
        messageBlockedCount = prefs.getInt(KEY_MESSAGE_BLOCKED, 0)
        rateLimitHitCount = prefs.getInt(KEY_RATE_LIMIT, 0)
        suspiciousResponseCount = prefs.getInt(KEY_SUSPICIOUS_RESPONSE, 0)
        lastCheckTimestamp = prefs.getLong(KEY_LAST_CHECK, 0L)
    }

    private fun saveState() {
        prefs.edit()
            .putBoolean(KEY_ACCOUNT_DISABLED, accountDisabledDetected)
            .putInt(KEY_MESSAGE_BLOCKED, messageBlockedCount)
            .putInt(KEY_RATE_LIMIT, rateLimitHitCount)
            .putInt(KEY_SUSPICIOUS_RESPONSE, suspiciousResponseCount)
            .putLong(KEY_LAST_CHECK, lastCheckTimestamp)
            .apply()
    }

    suspend fun checkBanStatus(): BanStatus = mutex.withLock {
        lastCheckTimestamp = System.currentTimeMillis()
        saveState()

        val indicators = mutableListOf<String>()
        var riskScore = 0

        if (accountDisabledDetected) {
            return@withLock BanStatus(
                isBanned = true,
                riskLevel = BanRiskLevel.BANNED,
                indicators = listOf("Account disabled signal detected"),
                lastChecked = lastCheckTimestamp,
                recommendedAction = "Account appears to be banned. Do not attempt to send messages."
            )
        }

        if (messageBlockedCount >= HIGH_BLOCK_THRESHOLD) {
            riskScore += 40
            indicators.add("High message block rate: $messageBlockedCount blocks")
        } else if (messageBlockedCount >= MEDIUM_BLOCK_THRESHOLD) {
            riskScore += 20
            indicators.add("Elevated message block rate: $messageBlockedCount blocks")
        }

        if (rateLimitHitCount >= HIGH_RATE_LIMIT_THRESHOLD) {
            riskScore += 30
            indicators.add("Frequent rate limiting: $rateLimitHitCount hits")
        } else if (rateLimitHitCount >= MEDIUM_RATE_LIMIT_THRESHOLD) {
            riskScore += 15
            indicators.add("Some rate limiting: $rateLimitHitCount hits")
        }

        if (suspiciousResponseCount >= HIGH_SUSPICIOUS_THRESHOLD) {
            riskScore += 30
            indicators.add("Multiple suspicious responses: $suspiciousResponseCount")
        } else if (suspiciousResponseCount >= MEDIUM_SUSPICIOUS_THRESHOLD) {
            riskScore += 15
            indicators.add("Some suspicious responses: $suspiciousResponseCount")
        }

        val riskLevel = when {
            riskScore >= 70 -> BanRiskLevel.DANGER
            riskScore >= 35 -> BanRiskLevel.WARNING
            else -> BanRiskLevel.SAFE
        }

        val recommendedAction = when (riskLevel) {
            BanRiskLevel.DANGER -> "Stop all messaging activities immediately"
            BanRiskLevel.WARNING -> "Reduce messaging frequency and add longer delays"
            BanRiskLevel.SAFE -> "Continue with caution"
            BanRiskLevel.BANNED -> "Account banned"
        }

        BanStatus(
            isBanned = false,
            riskLevel = riskLevel,
            indicators = indicators,
            lastChecked = lastCheckTimestamp,
            recommendedAction = recommendedAction
        )
    }

    suspend fun recordAccountDisabled() = mutex.withLock {
        accountDisabledDetected = true
        saveState()
    }

    suspend fun recordMessageBlocked() = mutex.withLock {
        messageBlockedCount++
        saveState()
    }

    suspend fun recordRateLimitHit() = mutex.withLock {
        rateLimitHitCount++
        saveState()
    }

    suspend fun recordSuspiciousResponse() = mutex.withLock {
        suspiciousResponseCount++
        saveState()
    }

    suspend fun reset() = mutex.withLock {
        accountDisabledDetected = false
        messageBlockedCount = 0
        rateLimitHitCount = 0
        suspiciousResponseCount = 0
        saveState()
    }

    companion object {
        private const val PREFS_NAME = "ban_detection_manager"
        private const val KEY_ACCOUNT_DISABLED = "account_disabled"
        private const val KEY_MESSAGE_BLOCKED = "message_blocked_count"
        private const val KEY_RATE_LIMIT = "rate_limit_count"
        private const val KEY_SUSPICIOUS_RESPONSE = "suspicious_response_count"
        private const val KEY_LAST_CHECK = "last_check_timestamp"

        private const val MEDIUM_BLOCK_THRESHOLD = 5
        private const val HIGH_BLOCK_THRESHOLD = 20
        private const val MEDIUM_RATE_LIMIT_THRESHOLD = 3
        private const val HIGH_RATE_LIMIT_THRESHOLD = 10
        private const val MEDIUM_SUSPICIOUS_THRESHOLD = 3
        private const val HIGH_SUSPICIOUS_THRESHOLD = 8
    }
}