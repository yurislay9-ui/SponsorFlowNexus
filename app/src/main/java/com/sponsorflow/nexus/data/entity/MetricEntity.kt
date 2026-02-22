/*
 * SponsorFlow Nexus v2.3 - Metric Entity
 */
package com.sponsorflow.nexus.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "metrics",
    indices = [Index(value = ["eventType"]), Index(value = ["timestamp"])]
)
data class MetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val eventData: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val EVENT_MESSAGE_SENT = "message_sent"
        const val EVENT_MESSAGE_RECEIVED = "message_received"
        const val EVENT_AUTO_REPLY = "auto_reply"
        const val EVENT_AI_GENERATION = "ai_generation"
        const val EVENT_ERROR = "error"
    }
}