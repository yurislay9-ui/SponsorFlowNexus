/*
 * SponsorFlow Nexus v2.4 - Click Lock (Anti Rapid Fire)
 * CORREGIDO: ConcurrentHashMap para thread-safety
 */
package com.sponsorflow.nexus.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object ClickLock {
    
    // CORREGIDO: ConcurrentHashMap para thread-safety
    private val locks = ConcurrentHashMap<String, Boolean>()
    private val jobs = ConcurrentHashMap<String, Job>()
    
    // Intentar adquirir lock (retorna false si ya está bloqueado)
    fun acquire(key: String = "default"): Boolean {
        // Usar putIfAbsent para operación atómica
        val previous = locks.putIfAbsent(key, true)
        return previous == null || previous == false
    }
    
    // Liberar lock manualmente
    fun release(key: String = "default") {
        locks[key] = false
        jobs[key]?.cancel()
        jobs.remove(key)
    }
    
    // Adquirir con auto-release después de millis
    fun acquireWithTimeout(
        key: String = "default",
        timeoutMs: Long = 3000,
        scope: CoroutineScope
    ): Boolean {
        if (!acquire(key)) return false
        
        jobs[key] = scope.launch {
            delay(timeoutMs)
            release(key)
        }
        return true
    }
    
    // Verificar si está bloqueado
    fun isLocked(key: String = "default"): Boolean {
        return locks[key] == true
    }
    
    // Ejecutar acción con protección
    inline fun <T> withLock(key: String = "default", action: () -> T): T? {
        if (!acquire(key)) return null
        return try {
            action()
        } finally {
            release(key)
        }
    }
    
    // Limpiar todos los locks
    fun clearAll() {
        locks.clear()
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }
}
