/*
 * Anti-Detection Constants
 */
package com.sponsorflow.nexus.antidetection

/**
 * Constantes anti-detección
 */
object AntiDetectionConstants {
    const val PREF_ANTIDETECTION = "nexus_antidetection"
    const val PREF_CONFIG = "config"
    const val PREF_DAILY_USAGE = "daily_usage"
    const val PREF_LAST_ACTIVITY = "last_activity"
    
    val PEAK_HOURS = listOf(9, 10, 11, 12, 17, 18, 19, 20)
}
