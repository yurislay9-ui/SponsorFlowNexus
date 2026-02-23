/*
 * SponsorFlow Nexus v1.0 - Sentiment Types
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.sentiment

enum class Sentiment {
    HAPPY, ANGRY, NEUTRAL, URGENT, CONFUSED
}

data class SentimentResult(
    val sentiment: Sentiment,
    val confidence: Float,
    val priority: Int,
    val action: String
)
