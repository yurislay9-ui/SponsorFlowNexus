/*
 * Smart Queue Constants
 */
package com.sponsorflow.nexus.queue

/**
 * Constantes del sistema de cola
 */
object SmartQueueConstants {

    // SharedPreferences keys
    const val PREF_CONFIG = "queue_config"
    const val PREF_STATES = "queue_states"
    const val PREF_HISTORY = "today_history"

    // Horas pico (reducir velocidad)
    val PEAK_HOURS = listOf(9, 10, 11, 12, 17, 18, 19, 20)

    // Delays por nivel de anti-detección
    fun getDelayForLevel(level: Int): Pair<Long, Long> {
        return when (level) {
            1 -> Pair(45000L, 120000L)  // Conservador
            2 -> Pair(30000L, 90000L)   // Normal
            3 -> Pair(15000L, 45000L)    // Alto
            else -> Pair(30000L, 90000L)
        }
    }

    // Límites por hora por nivel
    fun getHourlyLimitForLevel(level: Int): Int {
        return when (level) {
            1 -> 10   // Conservador
            2 -> 18   // Normal
            3 -> 25   // Alto
            else -> 18
        }
    }
}
