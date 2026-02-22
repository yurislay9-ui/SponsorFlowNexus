/*
 * SponsorFlow Nexus v2.3 - Session Manager
 */
package com.sponsorflow.nexus.account

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "nexus_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString("user_id", session.userId)
            .putString("email", session.email)
            .putString("display_name", session.displayName)
            .putString("id_token", session.idToken)
            .apply()
    }

    fun getSession(): UserSession? {
        val userId = prefs.getString("user_id", null) ?: return null
        return UserSession(
            userId = userId,
            email = prefs.getString("email", "") ?: "",
            displayName = prefs.getString("display_name", "") ?: "",
            idToken = prefs.getString("id_token", "") ?: ""
        )
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getString("user_id", null) != null

    fun getDeviceId(): String {
        var deviceId = prefs.getString("device_id", null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        return deviceId
    }
    
    // Obtener userId del token (para validación IDOR)
    fun getUserIdFromToken(): String? {
        return prefs.getString("user_id", null)
    }
    
    // Obtener token de sesión
    fun getToken(): String? {
        return prefs.getString("id_token", null)
    }
    
    // Verificar si el usuario solicitado es el mismo del token
    fun isOwner(requestedUserId: String): Boolean {
        val tokenUserId = getUserIdFromToken()
        return tokenUserId != null && tokenUserId == requestedUserId
    }
}
