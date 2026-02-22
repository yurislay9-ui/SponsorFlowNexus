/*
 * SponsorFlow Nexus v2.3 - Sentiment Analyzer
 * Plan: PRO y ENTERPRISE
 */
package com.sponsorflow.nexus.sentiment

class SentimentAnalyzer {

    private val keywords = mapOf(
        Sentiment.ANGRY to listOf("malo", "terrible", "estafa", "reclamo", "devolver", "queja"),
        Sentiment.URGENT to listOf("urgente", "ya", "inmediato", "rápido", "asap", "ahora"),
        Sentiment.HAPPY to listOf("gracias", "excelente", "genial", "perfecto", "recomiendo"),
        Sentiment.CONFUSED to listOf("no entiendo", "ayuda", "cómo", "duda", "explicar")
    )

    fun analyze(message: String): SentimentResult {
        val lower = message.lowercase()
        
        for ((sentiment, words) in keywords) {
            if (words.any { lower.contains(it) }) {
                return SentimentResult(
                    sentiment = sentiment,
                    confidence = 0.7f,
                    priority = getPriority(sentiment),
                    action = getAction(sentiment)
                )
            }
        }
        
        return SentimentResult(Sentiment.NEUTRAL, 0.5f, 3, "Responder normalmente")
    }

    private fun getPriority(s: Sentiment) = when (s) {
        Sentiment.ANGRY, Sentiment.URGENT -> 1
        Sentiment.CONFUSED -> 2
        Sentiment.NEUTRAL -> 3
        Sentiment.HAPPY -> 5
    }

    private fun getAction(s: Sentiment) = when (s) {
        Sentiment.ANGRY -> "Empatía y solución inmediata"
        Sentiment.URGENT -> "Atender urgentemente"
        Sentiment.CONFUSED -> "Ayuda y explicación"
        Sentiment.NEUTRAL -> "Responder normalmente"
        Sentiment.HAPPY -> "Agradecer y pedir reseña"
    }
}