/*
 * SponsorFlow Nexus - Analytics Manager
 * Mide todo: mensajes, conversiones, ROI, tiempo ahorrado
 * Cumple con anti-detección de WhatsApp
 */
package com.sponsorflow.nexus.analytics

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class MessageMetrics(
    val totalSent: Long = 0,
    val totalReceived: Long = 0,
    val aiResponses: Long = 0,
    val humanResponses: Long = 0,
    val totalCharsSent: Long = 0,
    val avgResponseTimeMs: Long = 0
)

data class ConversionMetrics(
    val totalConversions: Long = 0,
    val totalRevenue: Double = 0.0,
    val revenueByProduct: Map<String, Double> = emptyMap(),
    val conversionRate: Double = 0.0
)

data class TimeMetrics(
    val timeSavedMinutes: Long = 0,
    val messagesSaved: Long = 0,
    val estimatedCostSavings: Double = 0.0
)

data class AntiDetectionMetrics(
    val messagesToday: Int = 0,
    val lastMessageTimestamp: Long = 0,
    val avgDelayBetweenMessagesMs: Long = 0,
    val totalDelays: Int = 0,
    val randomDelaysAdded: Int = 0,
    val rateLimitWarnings: Int = 0
)

data class DailyAnalytics(
    val date: String,
    val messagesSent: Int = 0,
    val messagesReceived: Int = 0,
    val conversions: Int = 0,
    val revenue: Double = 0.0,
    val activeChats: Int = 0,
    val avgResponseTimeMs: Long = 0,
    val antiDetectionAlerts: Int = 0
)

data class ROIMetrics(
    val totalMessagesProcessed: Long = 0,
    val totalConversions: Long = 0,
    val totalRevenue: Double = 0.0,
    val timeSavedHours: Double = 0.0,
    val costVsHumanAgent: Double = 0.0,
    val roiPercentage: Double = 0.0,
    val customersServed: Int = 0,
    val avgMessagesPerCustomer: Double = 0.0
)

class AnalyticsManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_analytics", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Métricas en memoria (thread-safe)
    private val messageMetrics = ConcurrentHashMap<String, MessageMetrics>()
    private val dailyStats = ConcurrentHashMap<String, DailyAnalytics>()
    private val antiDetectionMetrics = AntiDetectionMetrics()

    // Contadores atómicos
    private val totalMessagesProcessed = AtomicLong(0)
    private val totalRevenue = AtomicLong(0)
    private val timeSavedMinutes = AtomicLong(0)

    // Configuración anti-detección
    companion object {
        // Límites WhatsApp (no oficial pero recomendado)
        const val MAX_MESSAGES_PER_DAY = 500
        const val MAX_MESSAGES_PER_HOUR = 60
        const val MIN_DELAY_BETWEEN_MSGS_MS = 3000L // 3 segundos mínimo
        const val MAX_DELAY_BETWEEN_MSGS_MS = 15000L // 15 segundos máximo
        const val RANDOM_DELAY_ADDITION_MS = 5000L // Hasta 5 segundos extra aleatorio

        // Costo promedio por hora de un agente humano
        const val HOURLY_HUMAN_COST = 5.0 // $5/hora (ejemplo)
    }

    // ==================== MENSAJES ====================

    /**
     * Registrar mensaje enviado
     * Cumple con anti-detección
     */
    fun logMessageSent(
        phone: String,
        isAIResponse: Boolean,
        responseTimeMs: Long,
        charCount: Int
    ) {
        val today = getTodayKey()

        // Actualizar métricas de mensajes
        messageMetrics.getOrPut(phone) { MessageMetrics() }.let { metrics ->
            if (isAIResponse) {
                messageMetrics[phone] = metrics.copy(
                    totalSent = metrics.totalSent + 1,
                    aiResponses = metrics.aiResponses + 1,
                    totalCharsSent = metrics.totalCharsSent + charCount,
                    avgResponseTimeMs = if (metrics.avgResponseTimeMs == 0L) responseTimeMs
                    else (metrics.avgResponseTimeMs + responseTimeMs) / 2
                )
            } else {
                messageMetrics[phone] = metrics.copy(
                    totalSent = metrics.totalSent + 1,
                    humanResponses = metrics.humanResponses + 1
                )
            }
        }

        // Actualizar estadísticas diarias
        dailyStats.getOrPut(today) { createDailyAnalytics(today) }.let { stats ->
            dailyStats[today] = stats.copy(
                messagesSent = stats.messagesSent + 1
            )
        }

        // Actualizar anti-detección
        updateAntiDetectionMetrics(responseTimeMs)

        // Incrementar contadores globales
        totalMessagesProcessed.incrementAndGet()
    }

    /**
     * Registrar mensaje recibido
     */
    fun logMessageReceived(phone: String) {
        val today = getTodayKey()

        messageMetrics.getOrPut(phone) { MessageMetrics() }.let { metrics ->
            messageMetrics[phone] = metrics.copy(
                totalReceived = metrics.totalReceived + 1
            )
        }

        dailyStats.getOrPut(today) { createDailyAnalytics(today) }.let { stats ->
            dailyStats[today] = stats.copy(
                messagesReceived = stats.messagesReceived + 1,
                activeChats = messageMetrics.keys.size
            )
        }
    }

    // ==================== CONVERSIONES ====================

    /**
     * Registrar una conversión/venta
     */
    fun logConversion(phone: String, amount: Double, productName: String) {
        val today = getTodayKey()

        // Calcular tiempo ahorrado (estimado: 5 min por mensaje que la IA处理)
        val messagesByAI = messageMetrics[phone]?.aiResponses ?: 0
        val timeSaved = (messagesByAI * 5).toLong() // 5 minutos por mensaje

        timeSavedMinutes.addAndGet(timeSaved)
        totalRevenue.addAndGet((amount * 100).toLong())

        dailyStats.getOrPut(today) { createDailyAnalytics(today) }.let { stats ->
            dailyStats[today] = stats.copy(
                conversions = stats.conversions + 1,
                revenue = stats.revenue + amount
            )
        }

        prefs.edit()
            .putLong("last_conversion_$phone", System.currentTimeMillis())
            .putDouble("total_revenue", totalRevenue.toDouble() / 100)
            .apply()
    }

    // ==================== ANTI-DETECCIÓN ====================

    /**
     * Verificar si se puede enviar mensaje (respetando límites)
     * Retorna delay sugerido en ms
     */
    fun canSendMessage(): Pair<Boolean, Long> {
        val today = getTodayKey()
        val stats = dailyStats[today] ?: createDailyAnalytics(today)

        // Verificar límite diario
        if (stats.messagesSent >= MAX_MESSAGES_PER_DAY) {
            return Pair(false, -1) // Límite alcanzado
        }

        // Verificar límite por hora (aproximado)
        val hourKey = "hour_${System.currentTimeMillis() / 3600000}"
        val messagesThisHour = prefs.getInt(hourKey, 0)

        if (messagesThisHour >= MAX_MESSAGES_PER_HOUR) {
            // Agregar aleatoriedad para evitar detección
            val delay = (MIN_DELAY_BETWEEN_MSGS_MS..MAX_DELAY_BETWEEN_MSGS_MS).random().toLong()
            return Pair(false, delay)
        }

        // Calcular delay basado en última actividad
        val now = System.currentTimeMillis()
        val lastMessage = antiDetectionMetrics.lastMessageTimestamp
        val timeSinceLastMsg = now - lastMessage

        var suggestedDelay = 0L

        if (lastMessage > 0) {
            if (timeSinceLastMsg < MIN_DELAY_BETWEEN_MSGS_MS) {
                suggestedDelay = MIN_DELAY_BETWEEN_MSGS_MS - timeSinceLastMsg
            }

            // Agregar delay aleatorio (anti-detección)
            val randomDelay = (0..RANDOM_DELAY_ADDITION_MS).random().toLong()
            suggestedDelay += randomDelay
        }

        return Pair(true, suggestedDelay)
    }

    /**
     * Actualizar métricas anti-detección
     */
    private fun updateAntiDetectionMetrics(responseTimeMs: Long) {
        val now = System.currentTimeMillis()
        val lastTimestamp = antiDetectionMetrics.lastMessageTimestamp

        val newTotalDelays = if (lastTimestamp > 0) {
            antiDetectionMetrics.totalDelays + 1
        } else 0

        val newAvgDelay = if (lastTimestamp > 0) {
            val delay = now - lastTimestamp
            (antiDetectionMetrics.avgDelayBetweenMessagesMs * (newTotalDelays - 1) + delay) / newTotalDelays
        } else 0L

        // Actualizar contador por hora
        val hourKey = "hour_${now / 3600000}"
        prefs.edit().putInt(hourKey, prefs.getInt(hourKey, 0) + 1).apply()

        // Actualizar métricas del día
        val today = getTodayKey()
        dailyStats.getOrPut(today) { createDailyAnalytics(today) }.let { stats ->
            dailyStats[today] = stats.copy(
                messagesToday = stats.messagesSent
            )
        }
    }

    /**
     * Obtener estado de anti-detección
     */
    fun getAntiDetectionStatus(): AntiDetectionMetrics {
        val today = getTodayKey()
        val stats = dailyStats[today]

        return AntiDetectionMetrics(
            messagesToday = stats?.messagesSent ?: 0,
            lastMessageTimestamp = antiDetectionMetrics.lastMessageTimestamp,
            avgDelayBetweenMessagesMs = antiDetectionMetrics.avgDelayBetweenMessagesMs,
            totalDelays = antiDetectionMetrics.totalDelays,
            randomDelaysAdded = antiDetectionMetrics.randomDelaysAdded,
            rateLimitWarnings = stats?.antiDetectionAlerts ?: 0
        )
    }

    /**
     * Alerta de anti-detección
     */
    fun addAntiDetectionAlert(reason: String) {
        val today = getTodayKey()
        dailyStats.getOrPut(today) { createDailyAnalytics(today) }.let { stats ->
            dailyStats[today] = stats.copy(
                antiDetectionAlerts = stats.antiDetectionAlerts + 1
            )
        }

        // Log para debugging
        prefs.edit()
            .putString("last_alert_$today", "$reason at ${System.currentTimeMillis()}")
            .apply()
    }

    // ==================== ROI ====================

    /**
     * Calcular métricas de ROI completas
     */
    fun calculateROI(): ROIMetrics {
        val totalMsgs = totalMessagesProcessed.get()
        val revenue = totalRevenue.toDouble() / 100
        val timeSaved = timeSavedMinutes.get()

        // Calcular costo vs agente humano
        val hoursSaved = timeSaved / 60.0
        val costSavings = hoursSaved * HOURLY_HUMAN_COST

        // Calcular ROI: (ganancia - costo) / costo * 100
        // Costo = valor del tiempo ahorrado (asumimos que la IA es "gratis" después de desarrollo)
        val roi = if (costSavings > 0) {
            ((revenue + costSavings - costSavings) / costSavings) * 100
        } else 0.0

        // Clientes únicos
        val customers = messageMetrics.keys.size

        // Promedio mensajes por cliente
        val avgMsgsPerCustomer = if (customers > 0) totalMsgs.toDouble() / customers else 0.0

        return ROIMetrics(
            totalMessagesProcessed = totalMsgs,
            totalConversions = dailyStats.values.sumOf { it.conversions }.toLong(),
            totalRevenue = revenue,
            timeSavedHours = hoursSaved,
            costVsHumanAgent = costSavings,
            roiPercentage = roi,
            customersServed = customers,
            avgMessagesPerCustomer = avgMsgsPerCustomer
        )
    }

    // ==================== DASHBOARD ====================

    /**
     * Obtener datos para el dashboard
     */
    fun getDashboardData(days: Int = 7): DashboardData {
        val analyticsList = mutableListOf<DailyAnalytics>()

        for (i in 0 until days) {
            val date = getDateKey(i)
            analyticsList.add(dailyStats[date] ?: createDailyAnalytics(date))
        }

        val roi = calculateROI()
        val antiDetection = getAntiDetectionStatus()

        return DashboardData(
            dailyStats = analyticsList.reversed(),
            roiMetrics = roi,
            antiDetectionStatus = antiDetection,
            messageMetrics = messageMetrics.values.toList()
        )
    }

    // ==================== HELPERS ====================

    private fun getTodayKey(): String = "day_${System.currentTimeMillis() / 86400000}"

    private fun getDateKey(daysAgo: Int): String {
        val ms = System.currentTimeMillis() - (daysAgo * 86400000L)
        return "day_${ms / 86400000}"
    }

    private fun createDailyAnalytics(date: String): DailyAnalytics {
        return DailyAnalytics(date = date)
    }

    // Persistir datos
    fun saveToPrefs() {
        val json = gson.toJson(dailyStats)
        prefs.edit().putString("daily_stats", json).apply()
    }

    fun loadFromPrefs() {
        val json = prefs.getString("daily_stats", null)
        if (json != null) {
            val loaded = gson.fromJson(json, dailyStats.javaClass)
            dailyStats.putAll(loaded)
        }
    }
}

data class DashboardData(
    val dailyStats: List<DailyAnalytics>,
    val roiMetrics: ROIMetrics,
    val antiDetectionStatus: AntiDetectionMetrics,
    val messageMetrics: List<MessageMetrics>
)
