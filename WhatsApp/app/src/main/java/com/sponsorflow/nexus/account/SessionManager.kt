/*
 * SponsorFlow Nexus v1.0 - Session Manager
 * CORREGIDO: Preservar device_id en logout
 */
package com.sponsorflow.nexus.account

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

class SessionManager @Inject constructor(@ApplicationContext context: Context) {

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

    // Keys para prefs
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_DEVICE_ID = "device_id"
    }

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_ID_TOKEN, session.idToken)
            .apply()
    }

    fun getSession(): UserSession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        return UserSession(
            userId = userId,
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            displayName = prefs.getString(KEY_DISPLAY_NAME, "") ?: "",
            idToken = prefs.getString(KEY_ID_TOKEN, "") ?: ""
        )
    }

    fun clearSession() {
        // CORREGIDO: Preservar device_id en logout para mantener identificación del dispositivo
        val deviceId = prefs.getString(KEY_DEVICE_ID, null)
        prefs.edit().clear().apply()
        // Restaurar device_id después de clear
        if (deviceId != null) {
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getString(KEY_USER_ID, null) != null

    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
    
    // Obtener userId del token (para validación IDOR)
    fun getUserIdFromToken(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
    
    // Obtener token de sesión
    fun getToken(): String? {
        return prefs.getString(KEY_ID_TOKEN, null)
    }
    
    // Verificar si el usuario solicitado es el mismo del token
    fun isOwner(requestedUserId: String): Boolean {
        val tokenUserId = getUserIdFromToken()
        return tokenUserId != null && tokenUserId == requestedUserId
    }
}
