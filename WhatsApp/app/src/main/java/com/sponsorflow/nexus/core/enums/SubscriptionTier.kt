/*
 * SponsorFlow Nexus v1.0 - Subscription Tiers
 * Skill: Mejores prácticas - Enum con propiedades
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
    val price: Double,
    val displayName: String
) {
    FREE(
        hasCustomPrompt = false, hasInventory = false, hasMemory = false,
        memoryLimit = 0, memoryChatsLimit = 3, smsLimit = 50, phoneNumbersLimit = 1,
        hasPlugins = false, hasPluginSDK = false, hasCategories = false,
        hasTTS = false, hasVoiceCloning = false, voiceCloneMaxSeconds = 0,
        hasAnalytics = false, hasHumanHandoff = false, hasTranslation = false, translationLanguages = 0,
        hasEcommerce = false, hasMultiChannel = false, maxChannels = 1,
        hasAITraining = false,
        price = 0.0, displayName = "Gratis"
    ),
    BASICO(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = 5, memoryChatsLimit = 5, smsLimit = 150, phoneNumbersLimit = 1,
        hasPlugins = true, hasPluginSDK = false, hasCategories = false,
        hasTTS = false, hasVoiceCloning = false, voiceCloneMaxSeconds = 0,
        hasAnalytics = true, hasHumanHandoff = false, hasTranslation = false, translationLanguages = 0,
        hasEcommerce = false, hasMultiChannel = false, maxChannels = 1,
        hasAITraining = false,
        price = 9.0, displayName = "Básico"
    ),
    AVANZADO(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = 10, memoryChatsLimit = 10, smsLimit = 300, phoneNumbersLimit = 1,
        hasPlugins = true, hasPluginSDK = false, hasCategories = true,
        hasTTS = true, hasVoiceCloning = false, voiceCloneMaxSeconds = 0,
        hasAnalytics = true, hasHumanHandoff = true, hasTranslation = true, translationLanguages = 3,
        hasEcommerce = false, hasMultiChannel = false, maxChannels = 1,
        hasAITraining = true,
        price = 19.0, displayName = "Avanzado"
    ),
    VIP(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = Int.MAX_VALUE, memoryChatsLimit = Int.MAX_VALUE, smsLimit = Int.MAX_VALUE, phoneNumbersLimit = 3,
        hasPlugins = true, hasPluginSDK = true, hasCategories = true,
        hasTTS = true, hasVoiceCloning = true, voiceCloneMaxSeconds = 12,
        hasAnalytics = true, hasHumanHandoff = true, hasTranslation = true, translationLanguages = 12,
        hasEcommerce = true, hasMultiChannel = true, maxChannels = 5,
        hasAITraining = true,
        price = 29.0, displayName = "VIP"
    );

    fun isAtLeast(other: SubscriptionTier): Boolean = ordinal >= other.ordinal

    companion object {
        fun fromName(name: String): SubscriptionTier =
            entries.find { it.name.equals(name, ignoreCase = true) } ?: FREE
    }
}