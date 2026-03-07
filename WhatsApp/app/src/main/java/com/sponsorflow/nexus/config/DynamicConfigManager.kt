package com.sponsorflow.nexus.config

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AppConfig(
    val minDelayMs: Long = 1000L,
    val maxDelayMs: Long = 5000L,
    val maxDailyMessages: Int = 500,
    val maxRetries: Int = 3,
    val enableBanDetection: Boolean = true,
    val enableRiskAssessment: Boolean = true,
    val batchSize: Int = 10,
    val pauseBetweenBatchesMs: Long = 30000L,
    val enableNotifications: Boolean = true,
    val darkMode: Boolean = false,
    val logLevel: String = "INFO"
)

class DynamicConfigManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    private fun loadConfig(): AppConfig {
        return AppConfig(
            minDelayMs = prefs.getLong(KEY_MIN_DELAY, 1000L),
            maxDelayMs = prefs.getLong(KEY_MAX_DELAY, 5000L),
            maxDailyMessages = prefs.getInt(KEY_MAX_DAILY, 500),
            maxRetries = prefs.getInt(KEY_MAX_RETRIES, 3),
            enableBanDetection = prefs.getBoolean(KEY_BAN_DETECTION, true),
            enableRiskAssessment = prefs.getBoolean(KEY_RISK_ASSESSMENT, true),
            batchSize = prefs.getInt(KEY_BATCH_SIZE, 10),
            pauseBetweenBatchesMs = prefs.getLong(KEY_BATCH_PAUSE, 30000L),
            enableNotifications = prefs.getBoolean(KEY_NOTIFICATIONS, true),
            darkMode = prefs.getBoolean(KEY_DARK_MODE, false),
            logLevel = prefs.getString(KEY_LOG_LEVEL, "INFO") ?: "INFO"
        )
    }

    private fun saveConfig(config: AppConfig) {
        prefs.edit()
            .putLong(KEY_MIN_DELAY, config.minDelayMs)
            .putLong(KEY_MAX_DELAY, config.maxDelayMs)
            .putInt(KEY_MAX_DAILY, config.maxDailyMessages)
            .putInt(KEY_MAX_RETRIES, config.maxRetries)
            .putBoolean(KEY_BAN_DETECTION, config.enableBanDetection)
            .putBoolean(KEY_RISK_ASSESSMENT, config.enableRiskAssessment)
            .putInt(KEY_BATCH_SIZE, config.batchSize)
            .putLong(KEY_BATCH_PAUSE, config.pauseBetweenBatchesMs)
            .putBoolean(KEY_NOTIFICATIONS, config.enableNotifications)
            .putBoolean(KEY_DARK_MODE, config.darkMode)
            .putString(KEY_LOG_LEVEL, config.logLevel)
            .apply()
    }

    suspend fun updateConfig(update: AppConfig.() -> AppConfig) = mutex.withLock {
        val newConfig = _config.value.update()
        _config.value = newConfig
        saveConfig(newConfig)
    }

    suspend fun setMinDelay(delayMs: Long) = updateConfig { copy(minDelayMs = delayMs) }
    suspend fun setMaxDelay(delayMs: Long) = updateConfig { copy(maxDelayMs = delayMs) }
    suspend fun setMaxDailyMessages(max: Int) = updateConfig { copy(maxDailyMessages = max) }
    suspend fun setMaxRetries(retries: Int) = updateConfig { copy(maxRetries = retries) }
    suspend fun setBanDetection(enabled: Boolean) = updateConfig { copy(enableBanDetection = enabled) }
    suspend fun setRiskAssessment(enabled: Boolean) = updateConfig { copy(enableRiskAssessment = enabled) }
    suspend fun setBatchSize(size: Int) = updateConfig { copy(batchSize = size) }
    suspend fun setPauseBetweenBatches(pauseMs: Long) = updateConfig { copy(pauseBetweenBatchesMs = pauseMs) }
    suspend fun setNotifications(enabled: Boolean) = updateConfig { copy(enableNotifications = enabled) }
    suspend fun setDarkMode(enabled: Boolean) = updateConfig { copy(darkMode = enabled) }
    suspend fun setLogLevel(level: String) = updateConfig { copy(logLevel = level) }

    fun getCurrentConfig(): AppConfig = _config.value

    suspend fun resetToDefaults() = mutex.withLock {
        val defaultConfig = AppConfig()
        _config.value = defaultConfig
        saveConfig(defaultConfig)
    }

    companion object {
        private const val PREFS_NAME = "dynamic_config"
        private const val KEY_MIN_DELAY = "min_delay_ms"
        private const val KEY_MAX_DELAY = "max_delay_ms"
        private const val KEY_MAX_DAILY = "max_daily_messages"
        private const val KEY_MAX_RETRIES = "max_retries"
        private const val KEY_BAN_DETECTION = "enable_ban_detection"
        private const val KEY_RISK_ASSESSMENT = "enable_risk_assessment"
        private const val KEY_BATCH_SIZE = "batch_size"
        private const val KEY_BATCH_PAUSE = "pause_between_batches_ms"
        private const val KEY_NOTIFICATIONS = "enable_notifications"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LOG_LEVEL = "log_level"
    }
}