/*
 * SponsorFlow Nexus v2.4 - Sentiment Types
 * CORREGIDO: Version actualizada a v2.4
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
