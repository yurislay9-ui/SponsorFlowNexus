/*
 * SponsorFlow Nexus v1.0 - Dynamic Config Manager
 * CORREGIDO: Asignar config al campo de instancia, usar Dispatchers.IO, integrado con Hilt
 */
package com.sponsorflow.nexus.config

import android.content.Context
import android.util.Log
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sponsorflow.nexus.network.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences("nexus_config", Context.MODE_PRIVATE)
    private val client = NetworkHelper.createClient()
    private val gson = Gson()
    
    // Campo de instancia - CORREGIDO: se asigna en fetchConfig
    @Volatile private var config: RemoteConfig? = null

    // Configuración por defecto hardcodeada (fallback final)
    private val defaultConfig = RemoteConfig(
        serverUrl = "",
        minAppVersion = "2.4.0", // CORREGIDO: 2.4.0 consistente
        maintenanceMode = false,
        google_client_id = "",
        app_signature = "",
        payment_config = PaymentConfig(),
        webhooks = WebhooksConfig(),
        ai_model = AIModelConfig(),
        github_updates = GitHubUpdatesConfig(),
        whatsapp_config = WhatsAppConfig()
    )

    suspend fun fetchConfig(): AppResult<RemoteConfig> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(getConfigUrl())
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val rawJson = response.body?.string() ?: ""
                    
                    // CORREGIDO: Asignar al campo de instancia, no a variable local
                    val parsedConfig = parseConfigSafe(rawJson)
                    this@DynamicConfigManager.config = parsedConfig
                    
                    // Guardar en cache
                    cacheConfig(parsedConfig, rawJson)
                    
                    AppResult.Success(parsedConfig)
                } else {
                    // Error HTTP - usar cache o default
                    AppResult.Success(getCachedOrFallback())
                }
            } catch (e: JsonSyntaxException) {
                Log.e("Nexus", "JSON malformado en config: ${e.message}")
                AppResult.Success(getCachedOrFallback())
            } catch (e: Exception) {
                Log.e("Nexus", "Error cargando config: ${e.message}")
                AppResult.Success(getCachedOrFallback())
            }
        }
    }

    // Parseo seguro con manejo de errores
    private fun parseConfigSafe(json: String): RemoteConfig {
        return try {
            if (json.isBlank()) {
                return defaultConfig
            }
            gson.fromJson(json, RemoteConfig::class.java) ?: defaultConfig
        } catch (e: JsonSyntaxException) {
            Log.e("Nexus", "Error parseando JSON: ${e.message}")
            defaultConfig
        } catch (e: Exception) {
            Log.e("Nexus", "Error inesperado parseando: ${e.message}")
            defaultConfig
        }
    }

    // Obtener cache o fallback
    fun getCachedOrFallback(): RemoteConfig {
        // Intentar cargar del cache
        val cachedJson = prefs.getString("config_json", null)
        
        if (!cachedJson.isNullOrBlank()) {
            val cached = parseConfigSafe(cachedJson)
            Log.d("Nexus", "Usando configuración cacheada")
            return cached
        }
        
        // Sin cache - usar default
        Log.d("Nexus", "Usando configuración por defecto")
        return defaultConfig
    }

    fun getCachedConfig(): RemoteConfig = getCachedOrFallback()
    
    // Forzar actualización de config (para botón reintentar)
    suspend fun refreshConfig(): AppResult<RemoteConfig> {
        return fetchConfig()
    }
    
    // Obtener config actual en memoria
    fun getCurrentConfig(): RemoteConfig {
        return config ?: getCachedOrFallback()
    }

    fun isFeatureEnabled(flag: String): Boolean {
        return prefs.getBoolean("feature_$flag", false)
    }

    // Guardar config completa
    private fun cacheConfig(config: RemoteConfig, rawJson: String) {
        prefs.edit()
            .putString("config_json", rawJson)
            .putString("server_url", config.serverUrl)
            .putString("min_version", config.minAppVersion)
            .putBoolean("maintenance", config.maintenanceMode)
            .putString("announcement", config.announcement)
            .putLong("last_update", System.currentTimeMillis())
            .apply()
    }

    companion object {
        fun getConfigUrl(): String = com.sponsorflow.nexus.BuildConfig.CONFIG_URL
    }
}

// Data classes con valores por defecto seguros
data class RemoteConfig(
    val serverUrl: String = "",
    val minAppVersion: String = "2.4.0",
    val maintenanceMode: Boolean = false,
    val announcement: String? = null,
    val google_client_id: String = "",
    val app_signature: String = "",
    val payment_config: PaymentConfig = PaymentConfig(),
    val webhooks: WebhooksConfig = WebhooksConfig(),
    val admin_webhooks: AdminWebhooksConfig = AdminWebhooksConfig(),
    val admin_commands: AdminCommandsConfig = AdminCommandsConfig(),
    val admin_banned_devices: List<String> = emptyList(),
    val heartbeat_interval_hours: Int = 1,
    val ai_model: AIModelConfig = AIModelConfig(),
    val github_updates: GitHubUpdatesConfig = GitHubUpdatesConfig(),
    val whatsapp_config: WhatsAppConfig = WhatsAppConfig()
)

data class PaymentConfig(
    val wallet_address: String = "",
    val usdt_contract: String = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
    val required_confirmations: Int = 20
)

data class WebhooksConfig(
    val nexus: String = "",
    val license: String = "",
    val payment: String = ""
)

data class AdminWebhooksConfig(
    val heartbeat_url: String = "",
    val error_url: String = ""
)

// CORREGIDO: Renombrar para evitar conflicto con com.sponsorflow.nexus.admin.AdminCommand
data class AdminCommandsConfig(
    val global: List<RemoteAdminCommand> = emptyList(),
    val devices: Map<String, List<RemoteAdminCommand>> = emptyMap()
)

// CORREGIDO: Clase renombrada para evitar shadowing con AdminCommand de AdminTypes.kt
data class RemoteAdminCommand(
    val type: String = "",
    val payload: String? = null
)

data class AIModelConfig(
    val download_url: String = "",
    val version: String = "1.0.0",
    val filename: String = "modelo.gguf",
    val size_mb: Int = 0
)

data class GitHubUpdatesConfig(
    val app_update_url: String = "",
    val whatsapp_fix_url: String = ""
)

data class WhatsAppConfig(
    val minVersion: String = "2.24.1",
    val updateRequired: Boolean = false
)
