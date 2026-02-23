/*
 * SponsorFlow Nexus v1.0 - Analytics Manager
 * Plan: OBSERVADOR, DESARROLLO, EMPRESARIO
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.analytics

import android.content.Context
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import java.util.Calendar

class AnalyticsManager(
    private val context: Context,
    private val tier: SubscriptionTier
) {
    companion object {
        // Límite de clientes almacenados para evitar consumo excesivo de memoria
        private const val MAX_CLIENTS_STORED = 100
    }
    
    private val prefs = context.getSharedPreferences("nexus_analytics", Context.MODE_PRIVATE)

    fun logMessage(phone: String) {
        incrementDaily()
        incrementTotal()
        updateClient(phone)
        updatePeakHour()
    }

    fun logResponseTime(timeMs: Long) {
        val total = prefs.getLong("response_total", 0L) + timeMs
        val count = prefs.getLong("response_count", 0L) + 1
        prefs.edit()
            .putLong("response_total", total)
            .putLong("response_count", count)
            .apply()
    }

    fun getAnalytics(): AnalyticsData {
        if (tier == SubscriptionTier.OBSERVADOR) return AnalyticsData()
        
        return AnalyticsData(
            messagesToday = getDaily(),
            messagesTotal = getTotal(),
            topClients = if (tier.ordinal >= SubscriptionTier.DESARROLLO.ordinal) getTopClients() else emptyList(),
            peakHours = if (tier.ordinal >= SubscriptionTier.DESARROLLO.ordinal) getPeakHours() else emptyList(),
            avgResponseTime = getAvgResponseTime()
        )
    }

    private fun incrementDaily() {
        val key = "day_${System.currentTimeMillis() / 86400000}"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    private fun incrementTotal() {
        prefs.edit().putLong("total", prefs.getLong("total", 0) + 1).apply()
    }

    /**
     * Actualiza estadísticas del cliente
     * CORREGIDO: Limitar número de clientes almacenados para evitar consumo de memoria
     */
    private fun updateClient(phone: String) {
        // Obtener solo las claves de cliente actuales
        val clientKeys = prefs.all.keys.filter { it.startsWith("client_") }.toList()
        
        // Si ya hay demasiados clientes, no agregar más
        if (clientKeys.size >= MAX_CLIENTS_STORED) {
            return
        }
        
        val count = prefs.getInt("client_$phone", 0) + 1
        prefs.edit()
            .putInt("client_$phone", count)
            .putLong("last_$phone", System.currentTimeMillis())
            .apply()
    }

    private fun updatePeakHour() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val key = "hour_$hour"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    private fun getDaily(): Int = prefs.getInt("day_${System.currentTimeMillis() / 86400000}", 0)
    private fun getTotal(): Long = prefs.getLong("total", 0)
    private fun getAvgResponseTime(): Long {
        val count = prefs.getLong("response_count", 0)
        return if (count > 0) prefs.getLong("response_total", 0) / count else 0
    }

    private fun getTopClients(): List<ClientStats> {
        return prefs.all.keys
            .filter { it.startsWith("client_") }
            .mapNotNull { key ->
                val phone = key.removePrefix("client_")
                ClientStats(phone, prefs.getInt(key, 0), prefs.getLong("last_$phone", 0))
            }
            .sortedByDescending { it.messageCount }
            .take(10)
    }

    private fun getPeakHours(): List<HourStats> {
        return (0..23).map { hour ->
            HourStats(hour, prefs.getInt("hour_$hour", 0))
        }.sortedByDescending { it.messageCount }.take(5)
    }
}
