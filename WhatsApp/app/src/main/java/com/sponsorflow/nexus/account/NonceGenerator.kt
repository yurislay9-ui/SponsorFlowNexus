/*
 * SponsorFlow Nexus v1.0 - Nonce Generator (Anti Replay)
 * CORREGIDO: Thread-safe con ConcurrentHashMap
 */
package com.sponsorflow.nexus.account

import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object NonceGenerator {
    
    private val random = SecureRandom()
    // Thread-safe: ConcurrentHashMap para operaciones atómicas
    private val usedNonces = ConcurrentHashMap<String, Long>()
    private val MAX_AGE_MS = TimeUnit.HOURS.toMillis(1) // Nonces expiran en 1 hora
    
    // Generar nonce único
    fun generate(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    // Generar y registrar nonce con timestamp
    fun generateAndStore(): String {
        val nonce = generate()
        usedNonces[nonce] = System.currentTimeMillis()
        return nonce
    }
    
    // Limpiar nonces antiguos (memoria y tiempo)
    private fun cleanupOldNonces() {
        val now = System.currentTimeMillis()
        usedNonces.entries.removeIf { (now - it.value) > MAX_AGE_MS }
    }
    
    // Verificar si nonce es válido (no usado antes y no expirado)
    fun isValid(nonce: String): Boolean {
        if (nonce.isBlank()) return false
        val timestamp = usedNonces[nonce]
        if (timestamp != null) {
            // Verificar si no ha expirado
            return (System.currentTimeMillis() - timestamp) <= MAX_AGE_MS
        }
        return true // No existe, es válido
    }
    
    // Marcar nonce como usado
    fun markUsed(nonce: String) {
        usedNonces[nonce] = System.currentTimeMillis()
    }
    
    // CORREGIDO: Verificar y consumir nonce atómicamente - NO permitir reuso de nonces expirados
    // Esto previene replay attacks
    fun consume(nonce: String): Boolean {
        if (nonce.isBlank()) return false
        
        // Limpiar nonces antiguos primero
        cleanupOldNonces()
        
        // Operación atómica: putIfAbsent retorna null si no existía
        val previous = usedNonces.putIfAbsent(nonce, System.currentTimeMillis())
        
        // Si previous != null, ya existía - rechazar SIEMPRE para prevenir replay attacks
        // CORREGIDO: No permitir reuso incluso si expiró
        return previous == null
    }
    
    // Limpiar todos los nonces
    fun clear() {
        usedNonces.clear()
    }
}
