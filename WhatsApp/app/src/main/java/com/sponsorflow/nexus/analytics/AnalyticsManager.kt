/*
 * Analytics Manager (Compact)
 */
package com.sponsorflow.nexus.analytics

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

class AnalyticsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_analytics", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dailyStats = ConcurrentHashMap<String, DailyStats>()

    fun recordMessageReceived(phone: String) { getTodayStats().messagesReceived++ }
    fun recordMessageSent(phone: String) { getTodayStats().messagesSent++ }
    fun recordRevenue(amount: Double) { getTodayStats().revenue += amount }
    fun recordConversion() { getTodayStats().conversions++ }

    fun getTodayStats(): DailyStats {
        val key = "day_${System.currentTimeMillis() / 86400000}"
        return dailyStats.getOrPut(key) { DailyStats(key) }
    }

    fun getStats(): AnalyticsData {
        val today = getTodayStats()
        return AnalyticsData(
            totalMessagesReceived = dailyStats.values.sumOf { it.messagesReceived },
            totalMessagesSent = dailyStats.values.sumOf { it.messagesSent },
            totalRevenue = dailyStats.values.sumOf { it.revenue },
            totalConversions = dailyStats.values.sumOf { it.conversions },
            today = today
        )
    }

    fun loadFromPrefs() { /* Load from prefs */ }
}
