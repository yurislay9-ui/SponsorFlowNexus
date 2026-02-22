/*
 * SponsorFlow Nexus v2.3 - Sentiment Types
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