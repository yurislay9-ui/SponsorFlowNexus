/*
 * SponsorFlow Nexus v2.4 - Authorization Interceptor (Anti-IDOR)
 */
package com.sponsorflow.nexus.network

import com.sponsorflow.nexus.account.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AuthorizationInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Obtener userId del token
        val tokenUserId = sessionManager.getUserIdFromToken()
        
        // Extraer userId de la URL si existe
        val requestUserId = extractUserIdFromPath(request.url.encodedPath)
        
        // Si hay userId en la URL y no coincide con el token, denegar
        if (requestUserId != null && tokenUserId != null) {
            if (requestUserId != tokenUserId) {
                return Response.Builder()
                    .request(request)
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(403)
                    .message("Forbidden - Access Denied")
                    .body("Forbidden: No tienes permiso para acceder a este recurso".toResponseBody())
                    .build()
            }
        }
        
        // Agregar header Authorization con token
        val authenticatedRequest = request.newBuilder()
            .addHeader("Authorization", "Bearer ${sessionManager.getToken()}")
            .addHeader("X-User-ID", tokenUserId ?: "")
            .addHeader("X-Request-ID", generateRequestId())
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
    
    // Extraer userId de paths como /api/user/101/inventory
    private fun extractUserIdFromPath(path: String): String? {
        val patterns = listOf(
            Regex("/user/([^/]+)"),
            Regex("/users/([^/]+)"),
            Regex("/api/user/([^/]+)")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(path)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
    
    private fun generateRequestId(): String {
        return "${System.currentTimeMillis()}-${java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999)}"
    }
}
