/*
 * Ban Detection Constants
 */
package com.sponsorflow.nexus.ban

/**
 * Constantes de detección de bloqouos
 */
object BanDetectionConstants {
    const val PREF_BAN = "nexus_ban"
    const val PREF_CONFIG = "ban_config"
    const val PREF_EVENTS = "phone_events"
    const val PREF_STATUS = "phone_status"
    
    const val DAYS_7 = 7 * 24 * 60 * 60 * 1000L
    const val DAYS_30 = 30 * 24 * 60 * 60 * 1000L
}
