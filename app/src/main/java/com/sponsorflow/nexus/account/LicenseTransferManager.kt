/*
 * SponsorFlow Nexus v2.3 - License Transfer Manager
 */
package com.sponsorflow.nexus.account

import com.sponsorflow.nexus.network.NetworkHelper
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object LicenseTransferManager {

    private val client = NetworkHelper.createClient()
    private val gson = Gson()

    sealed class TransferState {
        object Idle : TransferState()
        object Loading : TransferState()
        data class EmailSent(val email: String) : TransferState()
        data class RequiresVerification(val method: String) : TransferState()
        data class Success(val newDeviceId: String) : TransferState()
        data class Error(val message: String, val canRetry: Boolean = true) : TransferState()
    }

    /**
     * Solicita transferencia de licencia
     * CORREGIDO: withContext(Dispatchers.IO) para llamada bloqueante
     */
    suspend fun requestTransfer(
        licenseKey: String,
        email: String,
        sessionManager: SessionManager
    ): TransferState = withContext(Dispatchers.IO) {
        val configUrl = com.sponsorflow.nexus.BuildConfig.CONFIG_URL
        val newDeviceId = sessionManager.getDeviceId()

        val requestBody = gson.toJson(mapOf(
            "license_key" to licenseKey,
            "email" to email,
            "new_device_id" to newDeviceId,
            "action" to "request_transfer"
        ))

        try {
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
                TransferState.Error("Error ${response.code}")
            }
        } catch (e: Exception) {
            TransferState.Error(e.message ?: "Error de conexión", true)
        }
    }

    suspend fun verifyTransferCode(
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
            if (response.isSuccessful) TransferState.Success(newDeviceId)
            else TransferState.Error("Código inválido o expirado")
        } catch (e: Exception) {
            TransferState.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun linkToGoogleAccount(
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
            if (response.isSuccessful) TransferState.Success(deviceId)
            else TransferState.Error("Error al vincular")
        } catch (e: Exception) {
            TransferState.Error(e.message ?: "Error de conexión")
        }
    }

    suspend fun getLicenseInfoByEmail(email: String): LicenseInfoResult {
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
            } else LicenseInfoResult.NotFound
        } catch (e: Exception) {
            LicenseInfoResult.Error(e.message ?: "Error de conexión")
        }
    }
}