/*
 * SponsorFlow Nexus v2.3 - Nonce Generator (Anti Replay)
 */
package com.sponsorflow.nexus.account

import java.security.SecureRandom
import java.util.Base64

object NonceGenerator {
    
    private val random = SecureRandom()
    private val usedNonces = mutableSetOf<String>()
    
    // Generar nonce único
    fun generate(): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    // Generar y registrar nonce
    fun generateAndStore(): String {
        val nonce = generate()
        usedNonces.add(nonce)
        return nonce
    }
    
    // Verificar si nonce es válido (no usado antes)
    fun isValid(nonce: String): Boolean {
        if (nonce.isBlank()) return false
        if (usedNonces.contains(nonce)) return false
        return true
    }
    
    // Marcar nonce como usado
    fun markUsed(nonce: String) {
        usedNonces.add(nonce)
    }
    
    // Verificar y consumir nonce atómicamente
    fun consume(nonce: String): Boolean {
        if (!isValid(nonce)) return false
        markUsed(nonce)
        return true
    }
    
    // Limpiar nonces antiguos (memoria)
    fun clear() {
        usedNonces.clear()
    }
}