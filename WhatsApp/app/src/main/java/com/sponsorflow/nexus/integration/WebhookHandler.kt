/*
 * Webhook Handler - Receive events from n8n
 * GitHub controls the brain, n8n controls payments via Ngrok
 */
package com.sponsorflow.nexus.integration

import android.content.Context
import com.sponsorflow.nexus.config.NexusConfigManager
import com.google.gson.Gson
import kotlinx.coroutines.*

/**
 * Handle incoming webhook events from n8n
 */
class WebhookHandler(private val context: Context) {

    private val configManager = NexusConfigManager(context)
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Event listeners
    var onPaymentReceived: ((PaymentEvent) -> Unit)? = null
    var onSubscriptionChanged: ((SubscriptionEvent) -> Unit)? = null
    var onLicenseUpdated: ((LicenseEvent) -> Unit)? = null

    /**
     * Process incoming webhook from n8n
     */
    fun processWebhook(payload: String) {
        scope.launch {
            try {
                val event = gson.fromJson(payload, WebhookEvent::class.java)
                
                when (event.type) {
                    "payment.success" -> handlePaymentSuccess(event)
                    "payment.failed" -> handlePaymentFailed(event)
                    "subscription.created" -> handleSubscriptionCreated(event)
                    "subscription.upgraded" -> handleSubscriptionUpgraded(event)
                    "subscription.cancelled" -> handleSubscriptionCancelled(event)
                    "license.updated" -> handleLicenseUpdated(event)
                    else -> { /* Unknown event type */ }
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private fun handlePaymentSuccess(event: WebhookEvent) {
        val payment = PaymentEvent(
            transactionId = event.data?.get("transaction_id") ?: "",
            amount = event.data?.get("amount")?.toDoubleOrNull() ?: 0.0,
            currency = event.data?.get("currency") ?: "USD",
            tier = event.data?.get("tier") ?: "FREE"
        )
        onPaymentReceived?.invoke(payment)
    }

    private fun handlePaymentFailed(event: WebhookEvent) {
        // Handle failed payment
    }

    private fun handleSubscriptionCreated(event: WebhookEvent) {
        val subscription = SubscriptionEvent(
            tier = event.data?.get("tier") ?: "FREE",
            expiresAt = event.data?.get("expires_at")?.toLongOrNull() ?: 0L,
            status = "active"
        )
        onSubscriptionChanged?.invoke(subscription)
    }

    private fun handleSubscriptionUpgraded(event: WebhookEvent) {
        val subscription = SubscriptionEvent(
            tier = event.data?.get("tier") ?: "FREE",
            expiresAt = event.data?.get("expires_at")?.toLongOrNull() ?: 0L,
            status = "upgraded"
        )
        onSubscriptionChanged?.invoke(subscription)
    }

    private fun handleSubscriptionCancelled(event: WebhookEvent) {
        val subscription = SubscriptionEvent(
            tier = "FREE",
            expiresAt = 0L,
            status = "cancelled"
        )
        onSubscriptionChanged?.invoke(subscription)
    }

    private fun handleLicenseUpdated(event: WebhookEvent) {
        val license = LicenseEvent(
            licenseKey = event.data?.get("license_key") ?: "",
            tier = event.data?.get("tier") ?: "FREE",
            expiresAt = event.data?.get("expires_at")?.toLongOrNull() ?: 0L,
            active = event.data?.get("active")?.toBoolean() ?: false
        )
        onLicenseUpdated?.invoke(license)
    }

    fun cancel() = scope.cancel()
}

// Data classes for webhook events
data class WebhookEvent(
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val data: Map<String, String>?
)

data class PaymentEvent(
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val tier: String
)

data class SubscriptionEvent(
    val tier: String,
    val expiresAt: Long,
    val status: String
)

data class LicenseEvent(
    val licenseKey: String,
    val tier: String,
    val expiresAt: Long,
    val active: Boolean
)
