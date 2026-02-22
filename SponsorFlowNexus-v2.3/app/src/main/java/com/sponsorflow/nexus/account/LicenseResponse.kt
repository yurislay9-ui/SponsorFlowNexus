/*
 * SponsorFlow Nexus v2.3 - License Response (Anti-Replay)
 */
package com.sponsorflow.nexus.account

data class LicenseResponse(
    val status: String,
    val tier: String,
    val expiresAt: Long,
    val serverTimestamp: Long,
    val nonce: String,
    val features: List<String> = emptyList()
) {
    // Tiempo máximo de validez de respuesta (24 horas)
    companion object {
        private const val MAX_RESPONSE_AGE_MS = 24 * 60 * 60 * 1000L
    }
    
    // Verificar si la respuesta es válida (no es replay attack)
    fun isValid(nonceUsed: String): Boolean {
        // Verificar nonce coincide
        if (nonce != nonceUsed) return false
        
        // Verificar timestamp no muy antiguo
        val now = System.currentTimeMillis()
        val age = now - serverTimestamp
        if (age > MAX_RESPONSE_AGE_MS) return false
        
        // Verificar no expirada
        if (now > expiresAt) return false
        
        return true
    }
    
    // Verificar si la licencia está activa
    fun isActive(): Boolean {
        return status == "active" && System.currentTimeMillis() < expiresAt
    }
    
    // Tiempo restante en milisegundos
    fun getRemainingTime(): Long {
        return maxOf(0L, expiresAt - System.currentTimeMillis())
    }
}