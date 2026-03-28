/*
 * SponsorFlow Nexus - Integrations ViewModel
 */
package com.sponsorflow.nexus.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sponsorflow.nexus.integration.UserWebhookManager
import com.sponsorflow.nexus.integration.UserWebhookEvent

class IntegrationsViewModel(
    private val webhookManager: UserWebhookManager
) : ViewModel() {

    var webhookUrl by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var lastTestResult by mutableStateOf<String?>(null)
        private set

    init {
        webhookUrl = webhookManager.getUserWebhookUrl() ?: ""
    }

    fun updateUrl(url: String) {
        webhookUrl = url
    }

    fun saveUrl() {
        if (webhookUrl.isNotBlank()) {
            webhookManager.saveUserWebhookUrl(webhookUrl)
        } else {
            webhookManager.removeUserWebhook()
        }
    }

    suspend fun testWebhook(): Boolean {
        isLoading = true
        lastTestResult = null
        val result = webhookManager.testWebhook()
        isLoading = false
        lastTestResult = if (result.isSuccess) "✅ Webhook enviado correctamente"
                          else "❌ Error: ${result.exceptionOrNull()?.message}"
        return result.isSuccess
    }

    suspend fun sendPaymentWebhook(orderId: String, amount: Double, customerId: String): Boolean {
        val event = UserWebhookEvent(
            eventType = "payment_success",
            orderId = orderId,
            amount = amount,
            customerId = customerId,
            status = "completed",
            metadata = emptyMap()
        )
        return webhookManager.sendWebhook(event).isSuccess
    }
}