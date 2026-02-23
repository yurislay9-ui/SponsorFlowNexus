/*
 * SponsorFlow Nexus v2.4 - Retry Manager
 * CORREGIDO: Exponential backoff entre reintentos
 */
package com.sponsorflow.nexus.network

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.math.pow

sealed class RetryResult<T> {
    data class Success<T>(val data: T) : RetryResult<T>()
    data class NeedsRefresh<T>(val error: String) : RetryResult<T>()
    data class Failed<T>(val error: String) : RetryResult<T>()
}

class RetryManager<T>(
    private val maxRetries: Int = 3,
    private val refreshConfig: suspend () -> Boolean,
    private val initialDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 30000L
) {
    // CORREGIDO: Thread-safe con AtomicInteger
    private val retryCount = AtomicInteger(0)
    
    /**
     * Calcula el delay exponencial con jitter
     * CORREGIDO: Implementación de exponential backoff
     */
    private fun calculateDelay(attempt: Int): Long {
        val exponentialDelay = initialDelayMs * 2.0.pow(attempt.toDouble()).toLong()
        val cappedDelay = min(exponentialDelay, maxDelayMs)
        // Agregar jitter aleatorio (±25%)
        val jitter = (cappedDelay * 0.25 * Math.random()).toLong()
        return cappedDelay + jitter
    }
    
    suspend fun executeWithRetry(
        operation: suspend () -> T,
        shouldRefresh: (Exception) -> Boolean = { true }
    ): RetryResult<T> {
        retryCount.set(0)
        
        while (retryCount.get() < maxRetries) {
            try {
                val result = operation()
                return RetryResult.Success(result)
            } catch (e: Exception) {
                val currentAttempt = retryCount.incrementAndGet()
                
                if (currentAttempt < maxRetries) {
                    // CORREGIDO: Exponential backoff antes de reintentar
                    val delay = calculateDelay(currentAttempt)
                    delay(delay)
                    
                    if (shouldRefresh(e)) {
                        // Refrescar config antes de reintentar
                        val refreshed = refreshConfig()
                        if (!refreshed) {
                            return RetryResult.NeedsRefresh(
                                "No se pudo actualizar configuración"
                            )
                        }
                    }
                }
            }
        }
        
        return RetryResult.Failed("Error después de $maxRetries intentos")
    }
    
    fun reset() {
        retryCount.set(0)
    }
    
    fun getRetryCount(): Int = retryCount.get()
}
