/*
 * SponsorFlow Nexus - Human Behavior Simulator
 * Sistema de comportamiento humano para evitar detección de Meta
 * Versión limpia y funcional
 */
package com.sponsorflow.nexus.antidetection

import java.util.concurrent.ThreadLocalRandom

object HumanBehavior {

    /**
     * Delay de tipeo basado en longitud del texto
     * Simula el tiempo que un humano tardaría en escribir
     */
    fun getTypingDelay(text: String): Long {
        return try {
            if (text.isEmpty()) {
                return ThreadLocalRandom.current().nextLong(100, 300)
            }
            val baseDelay = 50L
            val charCount = text.length
            val variation = ThreadLocalRandom.current().nextLong(10, 100)
            val delay = (baseDelay * charCount) + variation
            delay.coerceAtMost(8000) // Máximo 8 segundos
        } catch (e: Exception) {
            ThreadLocalRandom.current().nextLong(100, 300)
        }
    }

    /**
     * Delay de respuesta (tiempo para "leer" y pensar)
     */
    fun getResponseDelay(): Long {
        return ThreadLocalRandom.current().nextLong(3000, 25000) // 3-25 segundos
    }

    /**
     * Verificar si está en horario activo (7am - 11pm)
     */
    fun isActiveTime(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 7..22
    }

    /**
     * Obtener horas activas
     */
    fun getActiveHours(): List<IntRange> {
        return listOf(7..12, 14..18, 19..22)
    }

    /**
     * Agregar error de tipeo menor (simular humano)
     */
    fun addTypo(text: String): String {
        if (text.isEmpty() || text.length < 3) return text
        
        // 10% chance de error de tipeo
        if (ThreadLocalRandom.current().nextInt(100) > 10) return text
        
        val typos = mapOf(
            "que" to "qe",
            "para" to "pra",
            "pero" to "pro",
            "estas" to "estaz",
            "hola" to "ola",
            "gracias" to "grasias",
            "buenos" to "buens",
            "dias" to "dia",
            "tengo" to "tngo",
            "puedo" to "pued",
            "quiero" to "quiro"
        )
        
        var result = text
        typos.forEach { (correct, typo) ->
            if (ThreadLocalRandom.current().nextBoolean()) {
                result = result.replace(correct, typo, ignoreCase = false)
            }
        }
        return result
    }

    /**
     * Formatear respuesta al estilo WhatsApp
     */
    fun formatWhatsAppStyle(text: String): String {
        if (text.isEmpty()) return text
        
        var result = text
        
        // 30% chance de empezar con minúscula
        if (ThreadLocalRandom.current().nextInt(100) < 30 && result.isNotEmpty()) {
            result = result.replaceFirstChar { it.lowercaseChar() }
        }
        
        // 30% chance de agregar emoji al final
        if (ThreadLocalRandom.current().nextInt(100) < 30) {
            val emojis = listOf("😊", "👍", "🙌", "✨", "🔥", "💪", "🎉", "❤️", "🙏")
            result += " ${emojis[ThreadLocalRandom.current().nextInt(emojis.size)]}"
        }
        
        return result
    }

    /**
     * Verificar si el texto suena a bot
     */
    fun soundsLikeBot(text: String): Boolean {
        val botPhrases = listOf(
            "Como modelo de lenguaje",
            "Soy un asistente de IA",
            "Mi entrenamiento",
            "Puedo ayudarte con",
            "Estoy diseñado para",
            "Gracias por tu pregunta",
            "Espero que esta respuesta",
            "a continuación",
            "en resumen"
        )
        
        return botPhrases.any { text.contains(it, ignoreCase = true) }
    }

    /**
     * Filtrar frases que delatan a la IA
     */
    fun filterBotPhrases(text: String): String {
        var filtered = text
        
        val phrases = listOf(
            "Como modelo de lenguaje",
            "Como modelo de IA",
            "Soy un asistente de IA",
            "I am an AI",
            "Mi entrenamiento",
            "Fue entrenado para",
            "Puedo ayudarte con",
            "Estoy diseñado para",
            "Tengo la capacidad de",
            "Gracias por tu pregunta",
            "Espero que esta respuesta",
            "Si tienes más preguntas"
        )
        
        phrases.forEach { phrase ->
            filtered = filtered.replace(Regex(phrase, RegexOption.IGNORE_CASE), "")
        }
        
        // Limpiar espacios extras
        filtered = filtered.replace(Regex("\\s+"), " ").trim()
        
        return filtered
    }

    /**
     * Variar respuestas para evitar patrones repetitivos
     */
    fun varyResponse(base: String, variations: List<String>): String {
        if (variations.isEmpty() || ThreadLocalRandom.current().nextInt(100) < 50) {
            return base
        }
        return variations[ThreadLocalRandom.current().nextInt(variations.size)]
    }
}
