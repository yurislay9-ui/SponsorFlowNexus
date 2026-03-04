/*
 * SponsorFlow Nexus v1.0 - Subscription Tiers
 * 
 * NOTA: La anti-detección es OBLIGATORIA para TODOS los planes
 * incluyendo el plan Gratis. El nivel de protección está integrado
 * en NexusAccessibilityService y no puede ser desactivado.
 */
package com.sponsorflow.nexus.core.enums

enum class SubscriptionTier(
    val hasCustomPrompt: Boolean,
    val hasInventory: Boolean,
    val hasMemory: Boolean,
    val memoryLimit: Int,
    val memoryChatsLimit: Int,
    val smsLimit: Int,
    val phoneNumbersLimit: Int,
    val extraPhoneNumberPrice: Double, // $5 por número extra
    val hasPlugins: Boolean,
    val hasPluginSDK: Boolean,
    val hasCategories: Boolean,
    val hasTTS: Boolean,
    val hasVoiceCloning: Boolean,
    val voiceCloneMaxSeconds: Int,
    val hasAnalytics: Boolean,
    val hasHumanHandoff: Boolean,
    val hasTranslation: Boolean,
    val translationLanguages: Int,
    val hasEcommerce: Boolean,
    val hasMultiChannel: Boolean,
    val maxChannels: Int,
    val hasAITraining: Boolean,
    val hasScheduledMessages: Boolean,
    val scheduledMessagesLimit: Int,
    val hasKeywordAutoResponse: Boolean,
    val keywordAutoResponseLimit: Int,
    // Anti-detección: OBLIGATORIA para todos los planes
    // Los límites determinan qué tan "conservador" es el comportamiento
    val antiDetectionLevel: Int, // 1=conservador, 2=normal, 3=alto
    val price: Double,
    val displayName: String
) {
    FREE(
        hasCustomPrompt = false, hasInventory = false, hasMemory = false,
        memoryLimit = 0, memoryChatsLimit = 3, smsLimit = 50, phoneNumbersLimit = 1,
        extraPhoneNumberPrice = 5.0,
        hasPlugins = false, hasPluginSDK = false, hasCategories = false,
        hasTTS = false, hasVoiceCloning = false, voiceCloneMaxSeconds = 0,
        hasAnalytics = false, hasHumanHandoff = false, hasTranslation = false, translationLanguages = 0,
        hasEcommerce = false, hasMultiChannel = false, maxChannels = 1,
        hasAITraining = false, hasScheduledMessages = false, scheduledMessagesLimit = 0,
        hasKeywordAutoResponse = true, keywordAutoResponseLimit = 5,
        antiDetectionLevel = 1, // Conservador: delay más largo, menos msgs
        price = 0.0, displayName = "Gratis"
    ),
    BASICO(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = 5, memoryChatsLimit = 5, smsLimit = 150, phoneNumbersLimit = 2,
        extraPhoneNumberPrice = 5.0,
        hasPlugins = true, hasPluginSDK = false, hasCategories = false,
        hasTTS = false, hasVoiceCloning = false, voiceCloneMaxSeconds = 0,
        hasAnalytics = true, hasHumanHandoff = false, hasTranslation = false, translationLanguages = 0,
        hasEcommerce = false, hasMultiChannel = false, maxChannels = 1,
        hasAITraining = false, hasScheduledMessages = true, scheduledMessagesLimit = 10,
        hasKeywordAutoResponse = true, keywordAutoResponseLimit = 20,
        antiDetectionLevel = 2, // Normal
        price = 9.0, displayName = "Básico"
    ),
    AVANZADO(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = 10, memoryChatsLimit = 10, smsLimit = 300, phoneNumbersLimit = 3,
        extraPhoneNumberPrice = 5.0,
        hasPlugins = true, hasPluginSDK = false, hasCategories = true,
        hasTTS = true, hasVoiceCloning = false, voiceCloneMaxSeconds = 0,
        hasAnalytics = true, hasHumanHandoff = true, hasTranslation = true, translationLanguages = 3,
        hasEcommerce = false, hasMultiChannel = false, maxChannels = 1,
        hasAITraining = true, hasScheduledMessages = true, scheduledMessagesLimit = 50,
        hasKeywordAutoResponse = true, keywordAutoResponseLimit = 50,
        antiDetectionLevel = 2, // Normal
        price = 19.0, displayName = "Avanzado"
    ),
    VIP(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = Int.MAX_VALUE, memoryChatsLimit = Int.MAX_VALUE, smsLimit = Int.MAX_VALUE, phoneNumbersLimit = 4,
        extraPhoneNumberPrice = 5.0,
        hasPlugins = true, hasPluginSDK = true, hasCategories = true,
        hasTTS = true, hasVoiceCloning = true, voiceCloneMaxSeconds = 12,
        hasAnalytics = true, hasHumanHandoff = true, hasTranslation = true, translationLanguages = 12,
        hasEcommerce = true, hasMultiChannel = true, maxChannels = 5,
        hasAITraining = true, hasScheduledMessages = true, scheduledMessagesLimit = Int.MAX_VALUE,
        hasKeywordAutoResponse = true, keywordAutoResponseLimit = Int.MAX_VALUE,
        antiDetectionLevel = 3, // Alto: más volumen permitido
        price = 29.0, displayName = "VIP"
    );

    fun isAtLeast(other: SubscriptionTier): Boolean = ordinal >= other.ordinal

    companion object {
        fun fromName(name: String): SubscriptionTier =
            entries.find { it.name.equals(name, ignoreCase = true) } ?: FREE
    }
}