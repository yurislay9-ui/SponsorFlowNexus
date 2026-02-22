/*
 * SponsorFlow Nexus v2.3 - License Transfer Manager
 */
package com.sponsorflow.nexus.account

import android.content.Context
import com.sponsorflow.nexus.network.NetworkHelper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object LicenseTransferManager {
    
    private val client = NetworkHelper.createClient()
    private val gson = Gson()
    
    // Estados de transferencia
    sealed class TransferState {
        object Idle : TransferState()
        object Loading : TransferState()
        data class EmailSent(val email: String) : TransferState()
        data class RequiresVerification(val method: String) : TransferState()
        data class Success(val newDeviceId: String) : TransferState()
        data class Error(val message: String, val canRetry: Boolean = true) : TransferState()
    }
    
    // Solicitar transferencia de licencia
    suspend fun requestTransfer(
        context: Context,
        licenseKey: String,
        email: String,
        sessionManager: SessionManager
    ): TransferState {
        val configUrl = com.sponsorflow.nexus.BuildConfig.CONFIG_URL
        val newDeviceId = sessionManager.getDeviceId()
        
        val requestBody = gson.toJson(mapOf(
            "license_key" to licenseKey,
            "email" to email,
            "new_device_id" to newDeviceId,
            "action" to "request_transfer"
        ))
        
        return try {
            val request = Request.Builder()
                .url("$configUrl/api/license/transfer/request")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val result = gson.fromJson(body, TransferResponse::class.java)
                
                when (result.status) {
                    "email_sent" -> TransferState.EmailSent(email)
                    "requires_verification" -> TransferState.RequiresVerification(result.method ?: "email")
                    "success" -> TransferState.Success(newDeviceId)
                    else -> TransferState.Error(result.error ?: "Error desconocido")
                }
            } else {
                val errorBody = response.body?.string() ?: ""
                val error = gson.fromJson(errorBody, ErrorResponse::class.java)
                TransferState.Error(error.message ?: "Error ${response.code}")
            }
        } catch (e: Exception) {
            TransferState.Error(e.message ?: "Error de conexión", true)
        }
    }
    
    // Verificar código de transferencia
    suspend fun verifyTransferCode(
        context: Context,
        licenseKey: String,
        code: String,
        sessionManager: SessionManager
    ): TransferState {
        val configUrl = com.sponsorflow.nexus.BuildConfig.CONFIG_URL
        val newDeviceId = sessionManager.getDeviceId()
        
        val requestBody = gson.toJson(mapOf(
            "license_key" to licenseKey,
            "verification_code" to code,
            "new_device_id" to newDeviceId,
            "action" to "verify_transfer"
        ))
        
        return try {
            val request = Request.Builder()
                .url("$configUrl/api/license/transfer/verify")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                TransferState.Success(newDeviceId)
            } else {
                TransferState.Error("Código inválido o expirado")
            }
        } catch (e: Exception) {
            TransferState.Error(e.message ?: "Error de conexión")
        }
    }
    
    // Vincular licencia a Google Account
    suspend fun linkToGoogleAccount(
        context: Context,
        licenseKey: String,
        googleIdToken: String,
        sessionManager: SessionManager
    ): TransferState {
        val configUrl = com.sponsorflow.nexus.BuildConfig.CONFIG_URL
        val deviceId = sessionManager.getDeviceId()
        
        val requestBody = gson.toJson(mapOf(
            "license_key" to licenseKey,
            "google_id_token" to googleIdToken,
            "device_id" to deviceId,
            "action" to "link_google"
        ))
        
        return try {
            val request = Request.Builder()
                .url("$configUrl/api/license/link-google")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                TransferState.Success(deviceId)
            } else {
                val errorBody = response.body?.string() ?: ""
                val error = gson.fromJson(errorBody, ErrorResponse::class.java)
                TransferState.Error(error.message ?: "Error al vincular")
            }
        } catch (e: Exception) {
            TransferState.Error(e.message ?: "Error de conexión")
        }
    }
    
    // Obtener información de licencia por email
    suspend fun getLicenseInfoByEmail(
        context: Context,
        email: String
    ): LicenseInfoResult {
        val configUrl = com.sponsorflow.nexus.BuildConfig.CONFIG_URL
        
        return try {
            val request = Request.Builder()
                .url("$configUrl/api/license/lookup?email=$email")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val info = gson.fromJson(body, LicenseLookupResponse::class.java)
                LicenseInfoResult.Found(info)
            } else {
                LicenseInfoResult.NotFound
            }
        } catch (e: Exception) {
            LicenseInfoResult.Error(e.message ?: "Error de conexión")
        }
    }
}

// Respuestas del servidor
data class TransferResponse(
    @SerializedName("status") val status: String,
    @SerializedName("method") val method: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("license_key") val licenseKey: String?
)

data class ErrorResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error_code") val errorCode: String?
)

data class LicenseLookupResponse(
    @SerializedName("has_license") val hasLicense: Boolean,
    @SerializedName("tier") val tier: String?,
    @SerializedName("expires_at") val expiresAt: Long?,
    @SerializedName("devices_count") val devicesCount: Int
)

sealed class LicenseInfoResult {
    data class Found(val info: LicenseLookupResponse) : LicenseInfoResult()
    object NotFound : LicenseInfoResult()
    data class Error(val message: String) : LicenseInfoResult()
}