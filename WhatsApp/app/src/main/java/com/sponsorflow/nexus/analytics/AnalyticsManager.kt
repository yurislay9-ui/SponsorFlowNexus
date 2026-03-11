/*
 * Analytics Manager (Compact)
 */
package com.sponsorflow.nexus.analytics

import android.content.Context
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

class AnalyticsManager(private val context: Context) {
    private val gson = Gson()
    private val dailyStats = ConcurrentHashMap<String, DailyStats>()

    private fun dayKey(): String = "day_${System.currentTimeMillis() / 86400000}"

    fun recordMessageReceived(phone: String) {
        val s = getTodayStats()
        val updated = s.copy(totalMessagesDelivered = s.totalMessagesDelivered + 1)
        dailyStats[dayKey()] = updated
    }

    fun recordMessageSent(phone: String) {
        val s = getTodayStats()
        val updated = s.copy(totalMessagesSent = s.totalMessagesSent + 1)
        dailyStats[dayKey()] = updated
    }

    fun recordRevenue(amount: Double) { /* opcional: persistir en prefs si lo necesitas */ }
    fun recordConversion() { /* opcional */ }

    fun getTodayStats(): DailyStats {
        val key = dayKey()
        return dailyStats.getOrPut(key) { DailyStats(date = key) }
    }

    fun getStats(): AnalyticsData {
        val today = getTodayStats()
        val totalMessages = dailyStats.values.sumOf { it.totalMessagesSent }
        return AnalyticsData(
            messagesToday = today.totalMessagesSent,
            messagesTotal = totalMessages.toLong(),
            topClients = emptyList(),
            peakHours = emptyList(),
            conversionRate = 0f,
            avgResponseTime = 0L
        )
    }

    fun loadFromPrefs() { /* TODO: cargar si es necesario */ }
}
