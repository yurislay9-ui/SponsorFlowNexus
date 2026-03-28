/*
 * Smart Queue Models - Additional data classes and enums
 */
package com.sponsorflow.nexus.queue

/**
 * Estado de procesamiento de un número
 */
data class NumberQueueState(
    val phoneNumber: String,
    var lastProcessedAt: Long = 0,
    var messagesToday: Int = 0,
    var messagesThisHour: Int = 0,
    var isPaused: Boolean = false,
    var pauseReason: String? = null,
    var pauseUntil: Long? = null
)

/**
 * Estadísticas de la cola
 */
data class QueueStats(
    val totalQueued: Int,
    val totalNumbers: Int,
    val pausedNumbers: Int,
    val messagesSentToday: Int,
    val avgDelayMs: Long
)
