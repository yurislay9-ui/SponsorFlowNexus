/*
 * SponsorFlow Nexus v1.0 - Analytics Data Models
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.analytics

data class AnalyticsData(
    val messagesToday: Int = 0,
    val messagesTotal: Long = 0,
    val topClients: List<ClientStats> = emptyList(),
    val peakHours: List<HourStats> = emptyList(),
    val conversionRate: Float = 0f,
    val avgResponseTime: Long = 0
)

data class ClientStats(
    val phone: String,
    val messageCount: Int,
    val lastContact: Long
)

data class HourStats(
    val hour: Int,
    val messageCount: Int
)

// ROIMetrics for ROI card display
data class ROIMetrics(
    val roiPercentage: Double = 0.0,
    val costVsHumanAgent: Double = 0.0,
    val totalMessagesProcessed: Int = 0,
    val totalConversions: Int = 0,
    val totalRevenue: Double = 0.0
)

// AntiDetectionMetrics for anti-detection card display
data class AntiDetectionMetrics(
    val messagesToday: Int = 0,
    val riskLevel: Int = 0,
    val totalBans: Int = 0
)
