/*
 * SponsorFlow Nexus v2.3 - Analytics Data Models
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