/*
 * SponsorFlow Nexus - User Webhook Manager
 * Permite a usuarios EMPRESARIO conectar con Zapier/N8N
 */
package com.sponsorflow.nexus.integration

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class WebhookEvent(
    val eventType: String,
    val orderId: String? = null,
    val amount: Double? = null,
    val currency: String = "USDT",
    val customerId: String? = null,
    val status: String = "completed",
    val metadata: Map<String, Any> = emptyMap()
)

class UserWebhookManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UserWebhookManager"
        private const val PREFS_NAME = "user_webhooks_encrypted"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // Guardar URL del usuario (cifrada)
    fun saveUserWebhookUrl(url: String) {
        encryptedPrefs.edit().putString("user_webhook_url", url).apply()
    }
    
    fun getUserWebhookUrl(): String? {
        return encryptedPrefs.getString("user_webhook_url", null)
    }
    
    fun hasUserWebhook(): Boolean = getUserWebhookUrl() != null
    
    fun removeUserWebhook() {
        encryptedPrefs.edit().remove("user_webhook_url").apply()
    }
    
    // Enviar webhook
    suspend fun sendWebhook(event: WebhookEvent): Result<Boolean> = withContext(Dispatchers.IO) {
        val url = getUserWebhookUrl()
        if (url.isNullOrBlank()) {
            return@withContext Result.failure(Exception("No webhook URL configured"))
        }
        
        try {
            val json = buildJsonPayload(event)
            val response = postJson(url, json)
            Log.d(TAG, "Webhook sent: ${event.eventType} -> $response")
            Result.success(true)
        } catch (e: IOException) {
            Log.e(TAG, "Webhook network error: ${e.message}")
            Result.failure(e)
        } catch (e: HttpException) {
            Log.e(TAG, "Webhook HTTP error: ${e.message}")
            Result.failure(e)
        } catch (e: JSONException) {
            Log.e(TAG, "Webhook JSON error: ${e.message}")
            Result.failure(e)
        } catch (e: SecurityException) {
            Log.e(TAG, "Webhook security error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Webhook unexpected error: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Test webhook
    suspend fun testWebhook(): Result<Boolean> {
        val testEvent = WebhookEvent(
            eventType = "test_webhook",
            orderId = "TEST-${System.currentTimeMillis()}",
            amount = 0.0,
            customerId = "test_user",
            status = "test"
        )
        return sendWebhook(testEvent)
    }
    
    private fun buildJsonPayload(event: WebhookEvent): String {
        val json = JSONObject()
        json.put("event_type", event.eventType)
        json.put("order_id", event.orderId ?: "N/A")
        json.put("amount", event.amount ?: 0.0)
        json.put("currency", event.currency)
        json.put("customer_id", event.customerId ?: "N/A")
        json.put("status", event.status)
        json.put("timestamp", java.time.Instant.now().toString())
        json.put("app_version", "2.4.0")
        
        // Metadata adicional
        val metadata = JSONObject()
        event.metadata.forEach { (k, v) -> metadata.put(k, v) }
        json.put("metadata", metadata)
        
        return json.toString()
    }
    
    private fun postJson(urlString: String, json: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "SponsorFlowNexus/2.4")
        conn.doOutput = true
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        
        conn.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
        
        val responseCode = conn.responseCode
        if (responseCode in 200..299) {
            return conn.inputStream.bufferedReader().readText()
        } else {
            throw Exception("HTTP $responseCode")
        }
    }
}