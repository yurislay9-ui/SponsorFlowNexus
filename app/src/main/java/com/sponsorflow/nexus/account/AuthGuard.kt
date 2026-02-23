/*
 * SponsorFlow Nexus v1.0 - Auth Guard (Protección de Sesión)
 * CORREGIDO: URL de API correcta, redirectToLogin, @Volatile, Dispatchers.IO
 */
package com.sponsorflow.nexus.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.sponsorflow.nexus.network.NetworkHelper
import com.sponsorflow.nexus.ui.auth.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

object AuthGuard {
    
    private val client = NetworkHelper.createClient()
    private val gson = Gson()
    
    // CORREGIDO: @Volatile para thread-safety
    @Volatile private var lastVerification: Long = 0
    private const val VERIFICATION_INTERVAL = 5 * 60 * 1000L // 5 minutos
    
    // CORREGIDO: URL del servidor desde config, no del JSON
    private fun getServerUrl(): String {
        return com.sponsorflow.nexus.BuildConfig.SERVER_URL
    }
    
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
        
        // CORREGIDO: withContext(Dispatchers.IO) para llamada de red
        return withContext(Dispatchers.IO) {
            try {
                val serverUrl = getServerUrl()
                val request = Request.Builder()
                    .url("$serverUrl/api/auth/verify")
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
    
    // CORREGIDO: Redirigir a LoginActivity correctamente
    private fun redirectToLogin(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
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