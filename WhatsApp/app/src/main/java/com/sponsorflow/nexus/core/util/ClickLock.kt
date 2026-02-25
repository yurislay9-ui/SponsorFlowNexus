/*
 * SponsorFlow Nexus v1.0 - Click Lock (Anti Rapid Fire)
 * CORREGIDO: ConcurrentHashMap con AtomicBoolean para thread-safety correcto
 */
package com.sponsorflow.nexus.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

object ClickLock {
    
    // CORREGIDO: ConcurrentHashMap con AtomicBoolean para operaciones atómicas correctas
    private val locks = ConcurrentHashMap<String, AtomicBoolean>()
    private val jobs = ConcurrentHashMap<String, Job>()
    
    // CORREGIDO: Usar computeIfAbsent + compareAndSet para operación atómica correcta
    fun acquire(key: String = "default"): Boolean {
        val lock = locks.computeIfAbsent(key) { AtomicBoolean(false) }
        return lock.compareAndSet(false, true)
    }
    
    // CORREGIDO: Liberar lock manualmente usando AtomicBoolean
    fun release(key: String = "default") {
        locks[key]?.set(false)
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
    
    // CORREGIDO: Verificar si está bloqueado usando AtomicBoolean
    fun isLocked(key: String = "default"): Boolean {
        return locks[key]?.get() == true
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
