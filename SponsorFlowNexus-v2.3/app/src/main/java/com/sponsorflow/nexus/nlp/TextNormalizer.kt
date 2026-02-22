/*
 * SponsorFlow Nexus - Text Normalizer
 * Pre-procesamiento NLP para entender mensajes con errores
 */
package com.sponsorflow.nexus.nlp

import java.text.Normalizer

object TextNormalizer {
    
    // Slang común en español
    private val slangMap = mapOf(
        "kiero" to "quiero", "k" to "que", "x fa" to "por favor",
        "xq" to "porque", "pq" to "porque", "dnd" to "donde",
        "cmo" to "como", "q" to "que", "va?" to "vale",
        "tn" to "tienes", "tngo" to "tengo", "msj" to "mensaje"
    )
    
    // Intenciones conocidas
    private val intents = mapOf(
        "pago" to listOf("pago", "pagar", "precio", "costo", "cuanto", "vale"),
        "ayuda" to listOf("ayuda", "ayudar", "soporte", "problema", "falla"),
        "producto" to listOf("producto", "articulo", "item", "catalogo", "ver"),
        "pedido" to listOf("pedido", "orden", "comprar", "compra"),
        "info" to listOf("info", "informacion", "saber", "detalles"),
        "saludo" to listOf("hola", "buenas", "hey", "saludos")
    )
    
    fun normalizeInput(text: String): String {
        var normalized = text.lowercase().trim()
        // Eliminar acentos
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
            .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
        // Eliminar caracteres repetidos (holaaaa -> hola)
        normalized = normalized.replace(Regex("(.)\\1{2,}"), "$1")
        // Reemplazar slang
        slangMap.forEach { (slang, correct) ->
            normalized = normalized.replace(slang, correct)
        }
        return normalized
    }
    
    fun detectIntent(text: String): String {
        val normalized = normalizeInput(text)
        var bestIntent = "desconocido"
        var bestScore = 0.0
        
        intents.forEach { (intent, keywords) ->
            keywords.forEach { keyword ->
                val score = similarity(normalized, keyword)
                if (score > bestScore && score > 0.6) {
                    bestScore = score
                    bestIntent = intent
                }
            }
        }
        return bestIntent
    }
    
    // Distancia de Levenshtein simplificada
    private fun similarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        val maxLen = maxOf(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        val dist = levenshtein(s1, s2)
        return 1.0 - (dist.toDouble() / maxLen)
    }
    
    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i-1] == s2[j-1]) 0 else 1
                dp[i][j] = minOf(dp[i-1][j] + 1, dp[i][j-1] + 1, dp[i-1][j-1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }
    
    fun buildPromptContext(originalText: String): String {
        val intent = detectIntent(originalText)
        val normalized = normalizeInput(originalText)
        return if (intent != "desconocido") {
            "Intención detectada: $intent. Mensaje original: $originalText"
        } else {
            "Mensaje: $originalText"
        }
    }
}