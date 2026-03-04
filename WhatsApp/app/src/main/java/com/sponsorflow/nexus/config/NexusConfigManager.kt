/*
 * Nexus Configuration Manager
 * Brain of the app - loads all config from GitHub
 * GitHub URL is HIDDEN - never exposed in UI
 */
package com.sponsorflow.nexus.config

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Configuration loaded from GitHub (hidden from user)
 * This is the BRAIN of the application
 */
class NexusConfigManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_config", Context.MODE_PRIVATE)
    private val client = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // GitHub URL - SECRET (loaded from resources, never hardcoded)
    // This should be in a secure config file
    private val GITHUB_CONFIG_URL: String by lazy {
        // Load from secure config or use placeholder
        // In production, this should be: getString(R.string.github_config_url)
        "https://raw.githubusercontent.com/yurislay9-ui/nexus-backend/refs/heads/main/config.json"
    }

    // Cache
    private var cachedConfig: JSONObject? = null
    private var lastFetchTime: Long = 0
    private val CACHE_DURATION_MS = 15 * 60 * 1000L // 15 minutes

    // ==================== MAIN CONFIG ====================

    /**
     * Get configuration value
     */
    fun get(key: String): Any? {
        return cachedConfig?.opt(key) ?: prefs.getString(key, null)
    }

    /**
     * Get string configuration
     */
    fun getString(key: String, default: String = ""): String {
        return get(key)?.toString() ?: prefs.getString(key, default) ?: default
    }

    /**
     * Get boolean configuration
     */
    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return get(key) as? Boolean ?: prefs.getBoolean(key, default)
    }

    /**
     * Get int configuration
     */
    fun getInt(key: String, default: Int = 0): Int {
        return (get(key) as? Number)?.toInt() ?: prefs.getInt(key, default)
    }

    // ==================== NETWORK CONFIG ====================

    fun getApiBaseUrl(): String = getString("api_base_url", "")
    fun getWhatsAppApiUrl(): String = getString("whatsapp_api_url", "")
    fun getWebhookUrl(): String = getString("webhook_url", "")

    // ==================== SUBSCRIPTION TIERS ====================

    fun getTierLimits(tier: String): Map<String, Int> {
        val tierObj = cachedConfig?.optJSONObject("tiers")?.optJSONObject(tier)
        return mapOf(
            "maxNumbers" to (tierObj?.optInt("maxNumbers") ?: 1),
            "maxMessagesPerDay" to (tierObj?.optInt("maxMessagesPerDay") ?: 50),
            "maxProducts" to (tierObj?.optInt("maxProducts") ?: 10),
            "antiDetectionLevel" to (tierObj?.optInt("antiDetectionLevel") ?: 1)
        )
    }

    // ==================== SYNC ====================

    /**
     * Fetch config from GitHub (called on app start)
     * This is done in background - user never sees the URL
     */
    fun syncConfig(onComplete: ((Boolean) -> Unit)? = null) {
        scope.launch {
            try {
                // Check cache first
                if (System.currentTimeMillis() - lastFetchTime < CACHE_DURATION_MS && cachedConfig != null) {
                    withContext(Dispatchers.Main) { onComplete?.invoke(true) }
                    return@launch
                }

                // Fetch from GitHub (BRAIN)
                val request = Request.Builder()
                    .url(GITHUB_CONFIG_URL)
                    .header("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        cachedConfig = JSONObject(body)
                        lastFetchTime = System.currentTimeMillis()
                        saveToPrefs(body)
                        withContext(Dispatchers.Main) { onComplete?.invoke(true) }
                        return@launch
                    }
                }
                
                // Fallback to cached
                withContext(Dispatchers.Main) { onComplete?.invoke(cachedConfig != null) }
            } catch (e: Exception) {
                // Use cached config if available
                withContext(Dispatchers.Main) { onComplete?.invoke(cachedConfig != null) }
            }
        }
    }

    /**
     * Force refresh config
     */
    fun refreshConfig() {
        lastFetchTime = 0
        syncConfig()
    }

    // ==================== SECURITY ====================

    /**
     * Get encryption keys from GitHub config
     */
    fun getEncryptionKey(): String = getString("encryption_key", "")

    /**
     * Get API keys (never exposed in UI)
     */
    fun getWhatsAppToken(): String = getString("whatsapp_token", "")
    fun getWhatsAppBusinessId(): String = getString("whatsapp_business_id", "")

    // ==================== FEATURE FLAGS ====================

    fun isFeatureEnabled(feature: String): Boolean {
        return cachedConfig?.optJSONObject("features")?.optBoolean(feature, false) == true
    }

    // ==================== VERSION ====================

    fun getMinAppVersion(): Int = getInt("min_app_version", 1)
    fun getLatestAppVersion(): Int = getInt("latest_app_version", 1)
    fun getUpdateUrl(): String = getString("update_url", "")

    // ==================== PERSISTENCE ====================

    private fun saveToPrefs(json: String) {
        prefs.edit().putString("cached_config", json).apply()
    }

    fun loadCachedConfig() {
        prefs.getString("cached_config", null)?.let { json ->
            try {
                cachedConfig = JSONObject(json)
            } catch (e: Exception) {
                // Invalid JSON, ignore
            }
        }
    }

    // ==================== UTILS ====================

    /**
     * Get all config as JSON (for debugging only - never show to user)
     */
    fun getDebugConfig(): String = cachedConfig?.toString() ?: "{}"

    /**
     * Check if config is loaded
     */
    fun isConfigLoaded(): Boolean = cachedConfig != null
}
