/*
 * SponsorFlow Nexus v1.0 - Human Behavior Simulator
 * CORREGIDO: Thread-safe con ThreadLocalRandom
 */
package com.sponsorflow.nexus.antidetection

import java.util.concurrent.ThreadLocalRandom

object HumanBehavior {

    fun getTypingDelay(text: String): Long {
        return try {
            if (text.isEmpty()) return ThreadLocalRandom.current().nextLong(100, 300)
            val baseDelay = 50L
            val charCount = text.length
            val variation = ThreadLocalRandom.current().nextLong(10, 100)
            val delay = (baseDelay * charCount) + variation
            delay.coerceAtMost(8000) // CAP: máximo 8 segundos
        } catch (e: IllegalArgumentException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: IllegalStateException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: SecurityException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: Exception) {
            ThreadLocalRandom.current().nextLong(100, 300)
        }
    }

    fun getReadTime(text: String): Long {
        return try {
            if (text.isBlank()) return ThreadLocalRandom.current().nextLong(100, 300)
            val words = text.split(" ").size
            val avgReadTime = 200L
            val variation = ThreadLocalRandom.current().nextLong(100, 500)
            (words * avgReadTime) + variation
        } catch (e: IllegalArgumentException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: IllegalStateException) {
            ThreadLocalRandom.current().nextLong(100, 300)
        } catch (e: Exception) {
            ThreadLocalRandom.current().nextLong(100, 300)
        }
    }

    fun getResponseDelay(): Long {
        return try {
            ThreadLocalRandom.current().nextLong(1000, 5000)
        } catch (e: IllegalArgumentException) {
            ThreadLocalRandom.current().nextLong(1000, 5000)
        } catch (e: IllegalStateException) {
            ThreadLocalRandom.current().nextLong(1000, 5000)
        } catch (e: Exception) {
            ThreadLocalRandom.current().nextLong(1000, 5000)
        }
    }

    fun addTypo(text: String): String {
        return try {
            // CORREGIDO: Mínimo 3 caracteres para evitar IllegalArgumentException en nextInt(1, 1)
            if (text.length < 3) return text
            if (ThreadLocalRandom.current().nextFloat() > 0.1f) return text
            val pos = ThreadLocalRandom.current().nextInt(1, text.length - 1)
            val chars = text.toMutableList()
            val temp = chars[pos]
            chars[pos] = chars[pos + 1]
            chars[pos + 1] = temp
            chars.joinToString("")
        } catch (e: IllegalArgumentException) {
            text
        } catch (e: IllegalStateException) {
            text
        } catch (e: Exception) {
            text
        }
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
