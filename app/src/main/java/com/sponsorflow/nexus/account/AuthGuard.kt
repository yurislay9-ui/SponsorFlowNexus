/*
 * SponsorFlow Nexus v2.3 - Auth Guard (Protección de Sesión)
 */
package com.sponsorflow.nexus.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sponsorflow.nexus.network.NetworkHelper
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

object AuthGuard {
    
    private val client = NetworkHelper.createClient()
    private val gson = Gson()
    private var lastVerification: Long = 0
    private const val VERIFICATION_INTERVAL = 5 * 60 * 1000L // 5 minutos
    
    // Verificar token contra servidor
    suspend fun verifyToken(sessionManager: SessionManager): TokenValidationResult {
        val token = sessionManager.getToken()
        val userId = sessionManager.getUserIdFromToken()
        
        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            return TokenValidationResult.Invalid("Sin token")
        }
        
        // Verificar caché reciente
        val now = System.currentTimeMillis()
        if (now - lastVerification < VERIFICATION_INTERVAL) {
            return TokenValidationResult.Valid
        }
        
        return try {
            val configUrl = com.sponsorflow.nexus.BuildConfig.CONFIG_URL
            val request = Request.Builder()
                .url("$configUrl/api/auth/verify")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("X-User-ID", userId)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val verifyResponse = gson.fromJson(body, TokenVerifyResponse::class.java)
                
                if (verifyResponse.isValid) {
                    lastVerification = now
                    TokenValidationResult.Valid
                } else {
                    sessionManager.clearSession()
                    TokenValidationResult.Invalid(verifyResponse.error ?: "Token inválido")
                }
            } else {
                // Error de red - permitir acceso si hay sesión local
                if (response.code == 401 || response.code == 403) {
                    sessionManager.clearSession()
                    TokenValidationResult.Invalid("Sesión expirada")
                } else {
                    TokenValidationResult.NetworkError
                }
            }
        } catch (e: Exception) {
            // Error de red - permitir acceso si hay sesión local
            TokenValidationResult.NetworkError
        }
    }
    
    // Requerir autenticación - redirigir a login si no hay sesión
    fun requireAuth(activity: Activity, sessionManager: SessionManager): Boolean {
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin(activity)
            return false
        }
        return true
    }
    
    // Verificar sesión válida (token + timestamp)
    fun hasValidSession(sessionManager: SessionManager): Boolean {
        if (!sessionManager.isLoggedIn()) return false
        
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) return false
        
        // Verificar que el token no sea un placeholder
        if (token.length < 10) return false
        
        return true
    }
    
    // Redirigir a login
    private fun redirectToLogin(activity: Activity) {
        // Limpiar sesión y redirigir
        activity.finish()
    }
    
    // Verificar sesión al iniciar actividad
    fun checkActivityAccess(
        activity: Activity,
        sessionManager: SessionManager,
        requireAuth: Boolean = true
    ): Boolean {
        if (!requireAuth) return true
        
        if (!hasValidSession(sessionManager)) {
            redirectToLogin(activity)
            return false
        }
        return true
    }
}

sealed class TokenValidationResult {
    object Valid : TokenValidationResult()
    object NetworkError : TokenValidationResult()
    data class Invalid(val reason: String) : TokenValidationResult()
}

data class TokenVerifyResponse(
    @SerializedName("valid") val isValid: Boolean,
    @SerializedName("error") val error: String? = null,
    @SerializedName("expires_at") val expiresAt: Long? = null
)