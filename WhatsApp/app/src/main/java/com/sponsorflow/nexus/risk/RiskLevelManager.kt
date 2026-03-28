package com.sponsorflow.nexus.risk

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class RiskAssessment(
    val level: RiskLevel,
    val score: Int,
    val factors: List<String> = emptyList(),
    val recommendation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class RiskLevelManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()

    private var consecutiveFailures: Int = 0
    private var recentErrorRate: Double = 0.0
    private var rapidSendingDetected: Boolean = false
    private var unusualPatternDetected: Boolean = false

    init {
        loadState()
    }

    private fun loadState() {
        consecutiveFailures = prefs.getInt(KEY_CONSECUTIVE_FAILURES, 0)
        recentErrorRate = prefs.getFloat(KEY_ERROR_RATE, 0f).toDouble()
        rapidSendingDetected = prefs.getBoolean(KEY_RAPID_SENDING, false)
        unusualPatternDetected = prefs.getBoolean(KEY_UNUSUAL_PATTERN, false)
    }

    private fun saveState() {
        prefs.edit()
            .putInt(KEY_CONSECUTIVE_FAILURES, consecutiveFailures)
            .putFloat(KEY_ERROR_RATE, recentErrorRate.toFloat())
            .putBoolean(KEY_RAPID_SENDING, rapidSendingDetected)
            .putBoolean(KEY_UNUSUAL_PATTERN, unusualPatternDetected)
            .apply()
    }

    suspend fun assessRisk(): RiskAssessment = mutex.withLock {
        val factors = mutableListOf<String>()
        var score = 0

        if (consecutiveFailures >= CRITICAL_FAILURE_THRESHOLD) {
            score += 40
            factors.add("High consecutive failures: $consecutiveFailures")
        } else if (consecutiveFailures >= HIGH_FAILURE_THRESHOLD) {
            score += 25
            factors.add("Elevated consecutive failures: $consecutiveFailures")
        } else if (consecutiveFailures >= MEDIUM_FAILURE_THRESHOLD) {
            score += 10
            factors.add("Some consecutive failures: $consecutiveFailures")
        }

        if (recentErrorRate >= CRITICAL_ERROR_RATE) {
            score += 30
            factors.add("Critical error rate: ${(recentErrorRate * 100).toInt()}%")
        } else if (recentErrorRate >= HIGH_ERROR_RATE) {
            score += 20
            factors.add("High error rate: ${(recentErrorRate * 100).toInt()}%")
        } else if (recentErrorRate >= MEDIUM_ERROR_RATE) {
            score += 10
            factors.add("Elevated error rate: ${(recentErrorRate * 100).toInt()}%")
        }

        if (rapidSendingDetected) {
            score += 20
            factors.add("Rapid sending pattern detected")
        }

        if (unusualPatternDetected) {
            score += 15
            factors.add("Unusual sending pattern detected")
        }

        val level = when {
            score >= 70 -> RiskLevel.CRITICAL
            score >= 40 -> RiskLevel.HIGH
            score >= 20 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val recommendation = when (level) {
            RiskLevel.CRITICAL -> "Stop sending immediately and review account status"
            RiskLevel.HIGH -> "Reduce sending rate significantly and add longer delays"
            RiskLevel.MEDIUM -> "Consider reducing sending frequency"
            RiskLevel.LOW -> "Normal operation, continue monitoring"
        }

        RiskAssessment(level = level, score = score, factors = factors, recommendation = recommendation)
    }

    suspend fun recordSuccess() = mutex.withLock {
        consecutiveFailures = (consecutiveFailures - 1).coerceAtLeast(0)
        updateErrorRate(false)
        saveState()
    }

    suspend fun recordFailure() = mutex.withLock {
        consecutiveFailures++
        updateErrorRate(true)
        saveState()
    }

    suspend fun setRapidSendingDetected(detected: Boolean) = mutex.withLock {
        rapidSendingDetected = detected
        saveState()
    }

    suspend fun setUnusualPatternDetected(detected: Boolean) = mutex.withLock {
        unusualPatternDetected = detected
        saveState()
    }

    suspend fun reset() = mutex.withLock {
        consecutiveFailures = 0
        recentErrorRate = 0.0
        rapidSendingDetected = false
        unusualPatternDetected = false
        saveState()
    }

    fun loadFromPrefs() {
        loadState()
    }

    fun saveToPrefs() {
        saveState()
    }

    private fun updateErrorRate(isError: Boolean) {
        recentErrorRate = recentErrorRate * ERROR_RATE_DECAY + (if (isError) 1.0 else 0.0) * (1 - ERROR_RATE_DECAY)
    }

    companion object {
        private const val PREFS_NAME = "risk_level_manager"
        private const val KEY_CONSECUTIVE_FAILURES = "consecutive_failures"
        private const val KEY_ERROR_RATE = "error_rate"
        private const val KEY_RAPID_SENDING = "rapid_sending"
        private const val KEY_UNUSUAL_PATTERN = "unusual_pattern"

        private const val MEDIUM_FAILURE_THRESHOLD = 3
        private const val HIGH_FAILURE_THRESHOLD = 7
        private const val CRITICAL_FAILURE_THRESHOLD = 15

        private const val MEDIUM_ERROR_RATE = 0.1
        private const val HIGH_ERROR_RATE = 0.25
        private const val CRITICAL_ERROR_RATE = 0.5

        private const val ERROR_RATE_DECAY = 0.8
    }
}