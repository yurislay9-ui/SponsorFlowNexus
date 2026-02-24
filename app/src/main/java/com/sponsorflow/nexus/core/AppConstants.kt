/*
 * SponsorFlow Nexus v1.0 - Application Constants
 * CORREGIDO: MEDIUM-011 - Constantes centralizadas
 */
package com.sponsorflow.nexus.core

/**
 * Constantes centralizadas de la aplicación.
 * Evita constantes mágicas dispersas en el código.
 */
object AppConstants {
    
    // ==================== Network ====================
    object Network {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 60L
        const val WRITE_TIMEOUT_SECONDS = 60L
        const val PING_TIMEOUT_SECONDS = 5L
        const val DOWNLOAD_TIMEOUT_SECONDS = 60L
        const val MAX_RETRY_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 1000L
        const val BACKOFF_MULTIPLIER = 2.0
    }
    
    // ==================== Cache ====================
    object Cache {
        const val MAX_FREE_MESSAGES = 100
        const val MAX_BASIC_MESSAGES = 500
        const val MAX_PRO_MESSAGES = 1000
        const val MAX_ENTERPRISE_MESSAGES = 10000
        const val MAX_MEMORY_ENTRIES = 50
        const val CACHE_EXPIRY_HOURS = 24
    }
    
    // ==================== AI ====================
    object AI {
        const val DEFAULT_MAX_TOKENS = 256
        const val DEFAULT_TEMPERATURE = 0.7f
        const val MAX_CONTEXT_LENGTH = 4096
        const val MIN_MODEL_SIZE_MB = 100
    }
    
    // ==================== Validation ====================
    object Validation {
        const val MIN_PHONE_LENGTH = 8
        const val MAX_NAME_LENGTH = 100
        const val MAX_MESSAGE_LENGTH = 4096
        const val MIN_PASSWORD_LENGTH = 8
        const val LICENSE_KEY_LENGTH = 32
    }
    
    // ==================== Payment ====================
    object Payment {
        const val MIN_PAYMENT_AMOUNT = 1L // En SUN (1 TRX = 1,000,000 SUN)
        const val TRON_DECIMALS = 6
        const val CONFIRMATION_BLOCKS = 19
    }
    
    // ==================== WorkManager ====================
    object WorkManager {
        const val CONFIG_SYNC_INTERVAL_HOURS = 6L
        const val LICENSE_CHECK_INTERVAL_HOURS = 24L
        const val HEARTBEAT_INTERVAL_MINUTES = 30L
    }
    
    // ==================== UI ====================
    object UI {
        const val ANIMATION_DURATION_MS = 300
        const val DEBOUNCE_DELAY_MS = 300L
        const val TYPING_INDICATOR_DELAY_MS = 1000L
    }
    
    // ==================== Security ====================
    object Security {
        const val NONCE_LENGTH = 16
        const val KEY_LENGTH = 32
        const val SALT_LENGTH = 16
        const val ITERATIONS = 65536
        const val MEMORY_COST = 3
        const val PARALLELISM = 4
    }
}