/*
 * Smart Queue Models - Data classes and enums
 */
package com.sponsorflow.nexus.queue

/**
 * Mensaje en cola esperando ser procesado
 */
data class QueuedMessage(
    val id: String,
    val phone: String,
    val customerName: String?,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: MessagePriority = MessagePriority.NORMAL,
    val retryCount: Int = 0,
    val contextData: Map<String, String>? = null
)

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
 * Resultado de agregar a cola
 */
data class QueueResult(
    val success: Boolean,
    val messageId: String? = null,
    val position: Int? = null,
    val estimatedDelayMs: Long? = null,
    val reason: String? = null
)

/**
 * Resultado de procesar siguiente mensaje
 */
data class ProcessResult(
    val hasMessage: Boolean,
    val message: QueuedMessage? = null,
    val delayMs: Long = 0,
    val shouldWait: Boolean = false,
    val waitReason: String? = null
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
