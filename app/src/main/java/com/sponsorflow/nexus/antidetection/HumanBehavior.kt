/*
 * SponsorFlow Nexus v2.4 - Human Behavior Simulator
 * CORREGIDO: Thread-safe con ThreadLocalRandom
 */
package com.sponsorflow.nexus.antidetection

import java.util.concurrent.ThreadLocalRandom

object HumanBehavior {

    fun getTypingDelay(text: String): Long {
        if (text.isEmpty()) return ThreadLocalRandom.current().nextLong(100, 300)
        val baseDelay = 50L
        val charCount = text.length
        val variation = ThreadLocalRandom.current().nextLong(10, 100)
        val delay = (baseDelay * charCount) + variation
        return delay.coerceAtMost(8000) // CAP: máximo 8 segundos
    }

    fun getReadTime(text: String): Long {
        if (text.isBlank()) return ThreadLocalRandom.current().nextLong(100, 300)
        val words = text.split(" ").size
        val avgReadTime = 200L
        val variation = ThreadLocalRandom.current().nextLong(100, 500)
        return (words * avgReadTime) + variation
    }

    fun getResponseDelay(): Long {
        return ThreadLocalRandom.current().nextLong(1000, 5000)
    }

    fun addTypo(text: String): String {
        // Seguridad: texto vacío o muy corto
        if (text.length < 2) return text
        if (ThreadLocalRandom.current().nextFloat() > 0.1f) return text
        val pos = ThreadLocalRandom.current().nextInt(1, text.length - 1)
        val chars = text.toMutableList()
        val temp = chars[pos]
        chars[pos] = chars[pos + 1]
        chars[pos + 1] = temp
        return chars.joinToString("")
    }

    fun getActiveHours(): List<IntRange> {
        return listOf(
            8..12,   // Mañana
            14..18,  // Tarde
            19..22   // Noche
        )
    }

    fun isActiveTime(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return getActiveHours().any { hour in it }
    }
}
