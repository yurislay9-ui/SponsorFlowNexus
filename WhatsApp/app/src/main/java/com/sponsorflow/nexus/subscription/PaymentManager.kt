/*
 * Payment Manager - Controlled by n8n via GitHub config
 * GitHub is the brain, n8n controls payments via Ngrok
 */
package com.sponsorflow.nexus.subscription

import android.content.Context
import com.sponsorflow.nexus.config.NexusConfigManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class PaymentManager(private val context: Context) {

    private val configManager = NexusConfigManager(context)
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Get n8n URL from GitHub config - NEVER hardcoded
    private fun getN8nBaseUrl(): String = configManager.getString("n8n_base_url", "")

    private fun isN8nEnabled(): Boolean = configManager.getBoolean("n8n_enabled", false)

    // ==================== PAYMENT WORKFLOWS ====================

    /**
     * Verify payment via n8n webhook (controlled by user)
     */
    fun verifyPayment(
        transactionId: String,
        amount: Double,
        currency: String,
        onResult: (PaymentResult) -> Unit
    ) {
        if (!isN8nEnabled()) {
            onResult(PaymentResult(false, "Payment system not configured"))
            return
        }

        scope.launch {
            try {
                val n8nUrl = "${getN8nBaseUrl()}/webhook/payment"
                
                val body = gson.toJson(mapOf(
                    "action" to "verify",
                    "transaction_id" to transactionId,
                    "amount" to amount,
                    "currency" to currency,
                    "timestamp" to System.currentTimeMillis()
                ))

                val request = Request.Builder()
                    .url(n8nUrl)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val result = gson.fromJson(response.body?.string(), Map::class.java)
                    val success = result["success"] as? Boolean ?: false
                    val message = result["message"] as? String ?: ""
                    withContext(Dispatchers.Main) {
                        onResult(PaymentResult(success, message))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(PaymentResult(false, "Payment verification failed"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(PaymentResult(false, e.message ?: "Error"))
                }
            }
        }
    }

    /**
     * Create subscription via n8n
     */
    fun createSubscription(
        email: String,
        tier: String,
        paymentMethod: String,
        onResult: (SubscriptionResult) -> Unit
    ) {
        if (!isN8nEnabled()) {
            onResult(SubscriptionResult(null, "Subscription not available"))
            return
        }

        scope.launch {
            try {
                val n8nUrl = "${getN8nBaseUrl()}/webhook/subscription"
                
                val body = gson.toJson(mapOf(
                    "action" to "create",
                    "email" to email,
                    "tier" to tier,
                    "payment_method" to paymentMethod,
                    "device_id" to getDeviceId()
                ))

                val request = Request.Builder()
                    .url(n8nUrl)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val result = gson.fromJson(response.body?.string(), Map::class.java)
                    val subscriptionId = result["subscription_id"] as? String
                    withContext(Dispatchers.Main) {
                        onResult(SubscriptionResult(subscriptionId, "Success"))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(SubscriptionResult(null, "Failed"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(SubscriptionResult(null, e.message ?: "Error"))
                }
            }
        }
    }

    /**
     * Validate license via n8n
     */
    fun validateLicense(
        licenseKey: String,
        onResult: (LicenseValidationResult) -> Unit
    ) {
        if (!isN8nEnabled()) {
            onResult(LicenseValidationResult(false, "License system not configured"))
            return
        }

        scope.launch {
            try {
                val n8nUrl = "${getN8nBaseUrl()}/webhook/license"
                
                val body = gson.toJson(mapOf(
                    "action" to "validate",
                    "license_key" to licenseKey,
                    "device_id" to getDeviceId()
                ))

                val request = Request.Builder()
                    .url(n8nUrl)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val result = gson.fromJson(response.body?.string(), Map::class.java)
                    val valid = result["valid"] as? Boolean ?: false
                    val tier = result["tier"] as? String ?: "FREE"
                    val expiresAt = (result["expires_at"] as? Number)?.toLong() ?: 0L
                    
                    withContext(Dispatchers.Main) {
                        onResult(LicenseValidationResult(valid, tier, expiresAt))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(LicenseValidationResult(false, "FREE", 0))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(LicenseValidationResult(false, "FREE", 0))
                }
            }
        }
    }

    // ==================== UTILS ====================

    private fun getDeviceId(): String {
        val prefs = context.getSharedPreferences("nexus_prefs", Context.MODE_PRIVATE)
        return prefs.getString("device_id", "") ?: ""
    }

    fun cancel() = scope.cancel()
}

data class PaymentResult(val success: Boolean, val message: String)
data class SubscriptionResult(val subscriptionId: String?, val message: String)
data class LicenseValidationResult(val valid: Boolean, val tier: String, val expiresAt: Long)
