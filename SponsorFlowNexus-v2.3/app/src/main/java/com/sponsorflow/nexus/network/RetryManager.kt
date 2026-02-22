/*
 * SponsorFlow Nexus v2.3 - Retry Manager
 */
package com.sponsorflow.nexus.network

sealed class RetryResult<T> {
    data class Success<T>(val data: T) : RetryResult<T>()
    data class NeedsRefresh<T>(val error: String) : RetryResult<T>()
    data class Failed<T>(val error: String) : RetryResult<T>()
}

class RetryManager<T>(
    private val maxRetries: Int = 3,
    private val refreshConfig: suspend () -> Boolean
) {
    private var retryCount = 0
    
    suspend fun executeWithRetry(
        operation: suspend () -> T,
        shouldRefresh: (Exception) -> Boolean = { true }
    ): RetryResult<T> {
        retryCount = 0
        
        while (retryCount < maxRetries) {
            try {
                val result = operation()
                return RetryResult.Success(result)
            } catch (e: Exception) {
                retryCount++
                
                if (shouldRefresh(e) && retryCount < maxRetries) {
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
        
        return RetryResult.Failed("Error después de $maxRetries intentos")
    }
    
    fun reset() {
        retryCount = 0
    }
    
    fun getRetryCount(): Int = retryCount
}