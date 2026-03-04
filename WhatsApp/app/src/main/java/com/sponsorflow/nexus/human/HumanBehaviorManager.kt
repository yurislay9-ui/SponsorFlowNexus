/*
 * Human Behavior Manager (Compact)
 */
package com.sponsorflow.nexus.human

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

class HumanBehaviorManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_human", Context.MODE_PRIVATE)
    private var config = HumanBehaviorConfig()

    fun getRandomDelay(): Long = Random.nextLong(config.minDelaySeconds * 1000L, config.maxDelaySeconds * 1000L)
    fun isWithinActiveHours(): Boolean { val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY); return hour in config.activeHourStart..config.activeHourEnd }
    fun shouldUseShortResponse(): Boolean = Random.nextDouble() < config.shortResponseChance
    fun getShortResponse(): String = ShortResponses.getRandom()
    fun filterBotPhrases(text: String): String { var filtered = text; BotPhrases.phrases.forEach { filtered = filtered.replace(it, "", ignoreCase = true) }; return filtered }
    fun canSendMessage(phone: String): Pair<Boolean, String?> {
        if (!config.enabled) return Pair(true, null)
        if (!isWithinActiveHours()) return Pair(false, "Outside active hours")
        return Pair(true, null)
    }
    fun setConfig(newConfig: HumanBehaviorConfig) { config = newConfig; saveToPrefs() }
    fun getConfig(): HumanBehaviorConfig = config
    private fun saveToPrefs() { prefs.edit().putString("config", config.toString()).apply() }
    fun loadFromPrefs() { /* Load config from prefs */ }
}
