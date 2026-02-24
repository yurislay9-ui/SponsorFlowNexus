/*
 * SponsorFlow Nexus v1.0 - Network Utilities
 * CORREGIDO: MEDIUM-006 - Retry Logic para llamadas de red
 */
package com.sponsorflow.nexus.network

import kotlinx.coroutines.delay
import java.io.IOException

/**
 * Utilidades para operaciones de red con manejo de reintentos
 */
object NetworkUtils {
    
    // Constantes para configuración de reintentos
    const val DEFAULT_MAX_RETRIES = 3
    const val DEFAULT_RETRY_DELAY_MS = 1000L
    const val DEFAULT_BACKOFF_MULTIPLIER = 2.0
    
    /**
     * Ejecuta un bloque de código con reintentos automáticos.
     * 
     * @param times Número máximo de intentos
     * @param delayMs Delay inicial entre reintentos en milisegundos
     * @param backoffMultiplier Multiplicador para el delay en cada reintento
     * @param block Bloque de código a ejecutar
     * @return Resultado del bloque si tiene éxito
     * @throws Exception si todos los intentos fallan
     */
    suspend fun <T> withRetry(
        times: Int = DEFAULT_MAX_RETRIES,
        delayMs: Long = DEFAULT_RETRY_DELAY_MS,
        backoffMultiplier: Double = DEFAULT_BACKOFF_MULTIPLIER,
        block: suspend () -> T
    ): T {
        var currentDelay = delayMs
        var lastException: Exception? = null
        
        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: IOException) {
                lastException = e
                if (attempt < times - 1) {
                    delay(currentDelay)
                    currentDelay = (currentDelay * backoffMultiplier).toLong()
                }
            } catch (e: Exception) {
                // Para excepciones que no son de red, no reintentar
                throw e
            }
        }
        
        throw lastException ?: IOException("All retry attempts failed")
    }
    
    /**
     * Ejecuta un bloque de código con reintentos, retornando null si falla.
     * Útil para operaciones no críticas.
     */
    suspend fun <T> withRetryOrNull(
        times: Int = DEFAULT_MAX_RETRIES,
        delayMs: Long = DEFAULT_RETRY_DELAY_MS,
        block: suspend () -> T
    ): T? {
        return try {
            withRetry(times, delayMs) { block() }
        } catch (e: Exception) {
            null
        }
    }
}