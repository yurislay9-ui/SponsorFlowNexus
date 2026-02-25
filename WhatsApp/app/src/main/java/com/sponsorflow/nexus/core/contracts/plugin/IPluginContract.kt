/*
 * SponsorFlow Nexus v1.0 - Plugin Contract Interface
 * Skill: Mejores prácticas - Sistema de plugins extensible
 */
package com.sponsorflow.nexus.core.contracts.plugin

import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.core.result.AppResult

interface IPluginContract {
    fun getPluginId(): String
    fun getVersion(): String
    fun getDisplayName(): String
    fun getDescription(): String
    fun getRequiredTier(): SubscriptionTier
    suspend fun execute(context: PluginContext): AppResult<PluginOutput>
    fun cleanup()
    fun isEnabled(): Boolean = true
}

data class PluginContext(
    val tier: SubscriptionTier,
    val data: Map<String, Any> = emptyMap(),
    val contactId: Long? = null
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = data[key] as? T

    fun hasRequiredTier(required: SubscriptionTier): Boolean = tier.isAtLeast(required)
}

data class PluginOutput(
    val success: Boolean,
    val data: Map<String, Any> = emptyMap(),
    val message: String? = null,
    val shouldRespond: Boolean = false,
    val responseText: String? = null
) {
    companion object {
        fun success() = PluginOutput(success = true)
        fun successWithResponse(text: String) = PluginOutput(success = true, shouldRespond = true, responseText = text)
        fun failure(msg: String) = PluginOutput(success = false, message = msg)
    }
}