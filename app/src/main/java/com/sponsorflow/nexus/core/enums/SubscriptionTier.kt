/*
 * SponsorFlow Nexus v2.3 - Subscription Tiers
 * Skill: Mejores prácticas - Enum con propiedades
 */
package com.sponsorflow.nexus.core.enums

enum class SubscriptionTier(
    val hasCustomPrompt: Boolean,
    val hasInventory: Boolean,
    val hasMemory: Boolean,
    val memoryLimit: Int,
    val hasPlugins: Boolean,
    val hasPluginSDK: Boolean,
    val hasCategories: Boolean,
    val price: Double,
    val displayName: String
) {
    FREE(
        hasCustomPrompt = false, hasInventory = false, hasMemory = false,
        memoryLimit = 0, hasPlugins = false, hasPluginSDK = false, hasCategories = false,
        price = 0.0, displayName = "Gratis"
    ),
    OBSERVADOR(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = 5, hasPlugins = true, hasPluginSDK = false, hasCategories = false,
        price = 9.0, displayName = "Observador"
    ),
    DESARROLLO(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = 20, hasPlugins = true, hasPluginSDK = false, hasCategories = true,
        price = 19.0, displayName = "Desarrollo"
    ),
    EMPRESARIO(
        hasCustomPrompt = true, hasInventory = true, hasMemory = true,
        memoryLimit = Int.MAX_VALUE, hasPlugins = true, hasPluginSDK = true, hasCategories = true,
        price = 29.0, displayName = "Empresario"
    );

    fun isAtLeast(other: SubscriptionTier): Boolean = ordinal >= other.ordinal

    companion object {
        fun fromName(name: String): SubscriptionTier =
            entries.find { it.name.equals(name, ignoreCase = true) } ?: FREE
    }
}