/*
 * SponsorFlow Nexus v1.0 - Subscription Gate
 * Anti-detección obligatorio para WhatsApp - VERSIÓN INTEGRADA
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
        return HumanBehavior.isActiveTime()
    }
    
    /**
     * Delay obligatorio antes de responder
     * VERSIÓN MEJORADA: Usa todos los sistemas anti-detección
     */
    suspend fun applyAntiDetection(message: String): Boolean {
        // 1. Verificar horario activo
        if (!HumanBehavior.isActiveTime()) return false
        
        // 2. Delay de "lectura" (tiempo que lleva leer el mensaje)
        kotlinx.coroutines.delay(HumanBehavior.getTypingDelay(message))
        
        // 3. Delay de respuesta (3-25 segundos variable)
        kotlinx.coroutines.delay(HumanBehavior.getResponseDelay())
        
        return true
    }
    
    /**
     * Procesar respuesta con anti-detección completa
     * Filtra frases de IA y formatea como humano
     */
    fun processResponse(text: String): String {
        // 1. Filtrar frases que delatan a la IA
        var filtered = HumanBehavior.filterBotPhrases(text)
        
        // 2. Aplicar variación para evitar patrones repetitivos
        filtered = PatternRotator.formatResponse(filtered)
        
        // 3. Formatear al estilo WhatsApp (emojis, minúsculas, etc)
        filtered = HumanBehavior.formatWhatsAppStyle(filtered)
        
        // 4. Posible error de tipeo menor (10% chance)
        filtered = HumanBehavior.addTypo(filtered)
        
        return filtered
    }
    
    /**
     * Verificar si el texto suena a bot (para logging)
     */
    fun isBotLike(text: String): Boolean {
        return HumanBehavior.soundsLikeBot(text)
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
     */
    fun checkAccess(feature: String): Boolean {
        try {
            val tier = getCurrentTierAsync()
            return when (feature.lowercase()) {
                "prompt", "custom_prompt" -> tier.hasCustomPrompt
                "inventory", "inventario" -> tier.hasInventory
                "memory", "memoria" -> tier.hasMemory
                "plugins" -> tier.hasPlugins
                "plugin_sdk", "sdk" -> tier.hasPluginSDK
                "categories", "categorias" -> tier.hasCategories
                "whatsapp", "auto_reply" -> true // Anti-detección obligatorio
                else -> false
            }
        } catch (e: IllegalArgumentException) {
            return false
        } catch (e: IllegalStateException) {
            return false
        } catch (e: Exception) {
            return false
        }
    }

    fun isGracePeriod(): Boolean = licenseValidator.isGracePeriodActive()

    fun getGraceDaysRemaining(): Int = licenseValidator.getRemainingGraceDays()

    /**
     * Obtiene tier de forma síncrona desde cache
     */
    private fun getCurrentTierAsync(): SubscriptionTier {
        return licenseValidator.getCachedLicenseInfo()?.tier ?: SubscriptionTier.FREE
    }
}
