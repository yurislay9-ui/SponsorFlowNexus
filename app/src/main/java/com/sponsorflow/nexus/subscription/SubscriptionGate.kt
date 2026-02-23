/*
 * SponsorFlow Nexus v1.0 - Subscription Gate
 * Anti-detección obligatorio para WhatsApp en todos los planes
 */
package com.sponsorflow.nexus.subscription

import com.sponsorflow.nexus.antidetection.HumanBehavior
import com.sponsorflow.nexus.antidetection.PatternRotator
import com.sponsorflow.nexus.nlp.TextNormalizer
import com.sponsorflow.nexus.nlp.PromptBuilder
import com.sponsorflow.nexus.core.contracts.security.ILicenseValidator
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.data.repositories.SubscriptionRepository

class SubscriptionGate(
    private val subscriptionRepo: SubscriptionRepository,
    private val licenseValidator: ILicenseValidator
) {
    // Anti-detección obligatorio - todos los planes deben usarlo
    fun canUseWhatsApp(): Boolean {
        return HumanBehavior.isActiveTime() // Solo responder en horas activas
    }
    
    // Delay obligatorio antes de responder
    suspend fun applyAntiDetection(message: String): Boolean {
        if (!HumanBehavior.isActiveTime()) return false
        kotlinx.coroutines.delay(HumanBehavior.getReadTime(message))
        kotlinx.coroutines.delay(HumanBehavior.getResponseDelay())
        return true
    }
    
    // Procesar respuesta con anti-detección
    fun processResponse(text: String): String {
        return HumanBehavior.addTypo(PatternRotator.formatResponse(text))
    }
    
    // NLP - Disponible para TODAS las suscripciones
    fun detectIntent(message: String): String {
        return TextNormalizer.detectIntent(message)
    }
    
    fun normalizeMessage(message: String): String {
        return TextNormalizer.normalizeInput(message)
    }
    
    fun buildAIPrompt(message: String): String {
        return PromptBuilder.buildPrompt(message)
    }

    suspend fun getCurrentTier(): SubscriptionTier {
        val subscription = subscriptionRepo.getActive().getOrNull()
        if (subscription == null || subscription.isExpired()) {
            if (licenseValidator.isGracePeriodActive()) {
                return licenseValidator.getCachedLicenseInfo()?.tier ?: SubscriptionTier.FREE
            }
            return SubscriptionTier.FREE
        }
        return SubscriptionTier.fromName(subscription.tier)
    }

    /**
     * Verifica acceso a funcionalidad
     * CORREGIDO: Usa getCurrentTierAsync para consistencia
     */
    fun checkAccess(feature: String): Boolean {
        val tier = getCurrentTierAsync()
        return when (feature.lowercase()) {
            "prompt", "custom_prompt" -> tier.hasCustomPrompt
            "inventory", "inventario" -> tier.hasInventory
            "memory", "memoria" -> tier.hasMemory
            "plugins" -> tier.hasPlugins
            "plugin_sdk", "sdk" -> tier.hasPluginSDK
            "categories", "categorias" -> tier.hasCategories
            "whatsapp", "auto_reply" -> true // Anti-detección obligatorio, todos tienen acceso
            else -> false
        }
    }

    fun isGracePeriod(): Boolean = licenseValidator.isGracePeriodActive()

    fun getGraceDaysRemaining(): Int = licenseValidator.getRemainingGraceDays()

    /**
     * Obtiene tier de forma síncrona desde cache
     * CORREGIDO: Nombre consistente con uso
     */
    private fun getCurrentTierAsync(): SubscriptionTier {
        return licenseValidator.getCachedLicenseInfo()?.tier ?: SubscriptionTier.FREE
    }
}
