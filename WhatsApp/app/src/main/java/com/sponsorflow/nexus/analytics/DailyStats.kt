package com.sponsorflow.nexus.analytics

data class DailyStats(
    val date: String,
    val totalMessagesSent: Int = 0,
    val totalMessagesDelivered: Int = 0,
    val totalMessagesFailed: Int = 0,
    val totalRecipients: Int = 0,
    val totalCampaigns: Int = 0,
    val averageDeliveryTimeMs: Long = 0L,
    val peakHour: Int = 0,
    val successRate: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    // UI display fields
    val messagesReceived: Int = 0,
    val messagesSent: Int = 0,
    val revenue: Double = 0.0
) {
    val failureRate: Double
        get() = if (totalMessagesSent > 0) totalMessagesFailed.toDouble() / totalMessagesSent.toDouble() else 0.0

    val deliveryRate: Double
        get() = if (totalMessagesSent > 0) totalMessagesDelivered.toDouble() / totalMessagesSent.toDouble() else 0.0
}