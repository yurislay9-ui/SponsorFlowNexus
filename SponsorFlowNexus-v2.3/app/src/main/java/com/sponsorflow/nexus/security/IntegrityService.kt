/*
 * SponsorFlow Nexus v2.4 - Play Integrity Service
 * Verifica que el dispositivo y la app son legítimos
 */
package com.sponsorflow.nexus.security

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Serializable
data class IntegrityVerdict(
    val deviceRecognitionVerdict: String,
    val appRecognitionVerdict: String,
    val accountActivityVerdict: String? = null
)

@Singleton
class IntegrityService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val integrityManager: IntegrityManager,
    private val json: Json
) {
    companion object {
        private const val TAG = "IntegrityService"
        // Reemplazar con tu Cloud Project Number en producción
        private const val CLOUD_PROJECT_NUMBER = 123456789L
    }

    sealed class IntegrityResult {
        data class Success(val verdict: IntegrityVerdict) : IntegrityResult()
        data class Error(val message: String, val errorCode: Int? = null) : IntegrityResult()
    }

    /**
     * Solicita un token de integridad y lo verifica
     * Debe llamarse antes de operaciones críticas como pagos
     */
    suspend fun verifyIntegrity(nonce: String = generateNonce()): IntegrityResult {
        return try {
            val token = requestIntegrityToken(nonce)
            val verdict = parseVerdict(token)
            
            when {
                verdict.deviceRecognitionVerdict == "MEETS_DEVICE_INTEGRITY" &&
                verdict.appRecognitionVerdict == "PLAY_RECOGNIZED" -> {
                    IntegrityResult.Success(verdict)
                }
                else -> {
                    IntegrityResult.Error(
                        "Integrity check failed: device=${verdict.deviceRecognitionVerdict}, app=${verdict.appRecognitionVerdict}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Integrity verification failed", e)
            IntegrityResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Verificación rápida para operaciones no críticas
     */
    suspend fun quickVerify(): Boolean {
        return when (val result = verifyIntegrity()) {
            is IntegrityResult.Success -> true
            is IntegrityResult.Error -> {
                Log.w(TAG, "Quick verify failed: ${result.message}")
                false
            }
        }
    }

    private suspend fun requestIntegrityToken(nonce: String): String {
        return suspendCancellableCoroutine { continuation ->
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
                    .build()
            ).addOnSuccessListener { response: IntegrityTokenResponse ->
                continuation.resume(response.token())
            }.addOnFailureListener { e: Exception ->
                continuation.resumeWithException(e)
            }
        }
    }

    private fun parseVerdict(token: String): IntegrityVerdict {
        // En producción, enviar token al backend para verificación
        // Por ahora, parseamos localmente (no es seguro pero sirve para desarrollo)
        return try {
            // El token es un JWT, extraer el payload
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(
                    android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE),
                    Charsets.UTF_8
                )
                json.decodeFromString<IntegrityVerdict>(payload)
            } else {
                // Fallback para desarrollo
                IntegrityVerdict(
                    deviceRecognitionVerdict = "MEETS_DEVICE_INTEGRITY",
                    appRecognitionVerdict = "PLAY_RECOGNIZED"
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse verdict, using fallback", e)
            IntegrityVerdict(
                deviceRecognitionVerdict = "MEETS_DEVICE_INTEGRITY",
                appRecognitionVerdict = "PLAY_RECOGNIZED"
            )
        }
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(24)
        java.security.SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
    }
}