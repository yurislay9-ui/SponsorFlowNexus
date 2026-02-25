/*
 * SponsorFlow Nexus v1.0 - Play Integrity Service
 * CORREGIDO: Cloud Project Number, fallback seguro, verificación server-side recomendada
 */
package com.sponsorflow.nexus.security

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.sponsorflow.nexus.network.NetworkHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
        // CORREGIDO: Obtener desde BuildConfig o config
        private fun getCloudProjectNumber(): Long {
            return try {
                com.sponsorflow.nexus.BuildConfig.CLOUD_PROJECT_NUMBER.takeIf { it > 0 } 
                    ?: 123456789L // Fallback - debe configurarse en producción
            } catch (e: SecurityException) {
                123456789L
            } catch (e: Exception) {
                123456789L
            }
        }
        
        // URL del endpoint de verificación server-side
        private const val VERIFICATION_ENDPOINT = "/api/integrity/verify"
    }

    sealed class IntegrityResult {
        data class Success(val verdict: IntegrityVerdict) : IntegrityResult()
        data class Error(val message: String, val errorCode: Int? = null) : IntegrityResult()
    }

    /**
     * Solicita un token de integridad y lo verifica
     * Debe llamarse antes de operaciones críticas como pagos
     * 
     * CORREGIDO: Enviar token al servidor para verificación segura
     */
    suspend fun verifyIntegrity(nonce: String = generateNonce()): IntegrityResult {
        return try {
            val token = requestIntegrityToken(nonce)
            
            // CORREGIDO: Intentar verificación server-side primero
            val serverResult = verifyWithServer(token, nonce)
            if (serverResult != null) {
                return serverResult
            }
            
            // Fallback local solo para desarrollo
            if (com.sponsorflow.nexus.BuildConfig.DEBUG) {
                val verdict = parseVerdictLocal(token)
                Log.w(TAG, "Using local verification (DEBUG only)")
                return evaluateVerdict(verdict)
            }
            
            // En producción sin server, denegar por seguridad
            IntegrityResult.Error("Server verification unavailable, denying for security")
            
        } catch (e: Exception) {
            Log.e(TAG, "Integrity verification failed", e)
            // CORREGIDO: En caso de error, denegar por defecto (fail-safe)
            if (com.sponsorflow.nexus.BuildConfig.DEBUG) {
                IntegrityResult.Success(
                    IntegrityVerdict(
                        deviceRecognitionVerdict = "MEETS_DEVICE_INTEGRITY",
                        appRecognitionVerdict = "PLAY_RECOGNIZED"
                    )
                )
            } else {
                IntegrityResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Verifica el token con el servidor backend
     * Este es el método seguro para producción
     */
    private suspend fun verifyWithServer(token: String, nonce: String): IntegrityResult? {
        return withContext(Dispatchers.IO) {
            try {
                val serverUrl = com.sponsorflow.nexus.BuildConfig.SERVER_URL
                // CORREGIDO: Usar MapSerializer explícito para serializar Map
                val requestBody = json.encodeToString(
                    MapSerializer(String.serializer(), String.serializer()),
                    mapOf("token" to token, "nonce" to nonce)
                ).toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url("$serverUrl$VERIFICATION_ENDPOINT")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                val response = NetworkHelper.createClient().newCall(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val result = json.decodeFromString<ServerIntegrityResponse>(body)
                    
                    if (result.valid) {
                        IntegrityResult.Success(
                            IntegrityVerdict(
                                deviceRecognitionVerdict = "MEETS_DEVICE_INTEGRITY",
                                appRecognitionVerdict = "PLAY_RECOGNIZED"
                            )
                        )
                    } else {
                        IntegrityResult.Error(result.error ?: "Server validation failed")
                    }
                } else {
                    null // Fallback a local
                }
            } catch (e: Exception) {
                Log.w(TAG, "Server verification failed, using fallback", e)
                null
            }
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
            val projectNumber = getCloudProjectNumber()
            
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .setCloudProjectNumber(projectNumber)
                    .build()
            ).addOnSuccessListener { response: IntegrityTokenResponse ->
                continuation.resume(response.token())
            }.addOnFailureListener { e: Exception ->
                continuation.resumeWithException(e)
            }
        }
    }

    // CORREGIDO: Evaluación centralizada
    private fun evaluateVerdict(verdict: IntegrityVerdict): IntegrityResult {
        return when {
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
    }

    // CORREGIDO: Parseo local con fallback seguro (deniega por defecto)
    private fun parseVerdictLocal(token: String): IntegrityVerdict {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payload = String(
                    android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE),
                    Charsets.UTF_8
                )
                json.decodeFromString<IntegrityVerdict>(payload)
            } else {
                // Fallback seguro - denegar
                createDenyVerdict("Invalid token format")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse verdict", e)
            // Fallback seguro - denegar en producción
            createDenyVerdict("Parse error: ${e.message}")
        }
    }
    
    private fun createDenyVerdict(reason: String): IntegrityVerdict {
        Log.w(TAG, "Creating deny verdict: $reason")
        return IntegrityVerdict(
            deviceRecognitionVerdict = "VERIFY_FAILED",
            appRecognitionVerdict = "UNRECOGNIZED"
        )
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(24)
        java.security.SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
    }
}

@Serializable
data class ServerIntegrityResponse(
    val valid: Boolean,
    val error: String? = null
)
