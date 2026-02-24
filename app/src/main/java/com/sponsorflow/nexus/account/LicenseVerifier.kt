/*
 * SponsorFlow Nexus v1.0 - License Verifier
 * CORREGIDO: Versión desde BuildConfig, Mutex para cachedLicense, Dispatchers.IO
 */
package com.sponsorflow.nexus.account

import android.content.Context
import com.sponsorflow.nexus.core.contracts.security.ILicenseValidator
import com.sponsorflow.nexus.core.contracts.security.LicenseInfo
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class LicenseVerifier(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val serverUrl: String
) : ILicenseValidator {

    // CORREGIDO: Timeouts configurados
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    // CORREGIDO: Mutex para thread-safety
    private val cacheMutex = Mutex()
    private var cachedLicense: LicenseInfo? = null

    // Obtener versión desde BuildConfig
    private fun getAppVersion(): String {
        return try {
            com.sponsorflow.nexus.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "2.4.0" // Fallback
        }
    }

    override suspend fun validate(licenseKey: String): AppResult<LicenseInfo> {
        // CORREGIDO: withContext(Dispatchers.IO)
        return withContext(Dispatchers.IO) {
            try {
                val deviceId = sessionManager.getDeviceId()
                val appVersion = getAppVersion()
                
                val body = gson.toJson(mapOf(
                    "licenseKey" to licenseKey,
                    "deviceId" to deviceId,
                    "appVersion" to appVersion
                ))
                
                val request = Request.Builder()
                    .url("$serverUrl/api/license/validate")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                
                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val info = parseLicense(response.body?.string() ?: "")
                    // CORREGIDO: Actualizar cache con mutex
                    cacheMutex.withLock {
                        cachedLicense = info
                    }
                    AppResult.Success(info)
                } else {
                    AppResult.Error(AppError.LicenseError("Licencia inválida"))
                }
            } catch (e: Exception) {
                AppResult.Error(AppError.fromException(e))
            }
        }
    }

    // CORREGIDO: Leer cachedLicense dentro del mutex para evitar race condition
    override suspend fun refresh(): AppResult<LicenseInfo> {
        val key = cacheMutex.withLock {
            cachedLicense?.key
        } ?: return AppResult.Error(AppError.LicenseError("No hay licencia"))
        return validate(key)
    }

    override fun isGracePeriodActive(): Boolean {
        val license = cachedLicense ?: return false
        return license.isExpired() && getRemainingGraceDays() > 0
    }

    // CORREGIDO: Sin returns confusos en lambdas
    override fun getRemainingGraceDays(): Int {
        val license = cachedLicense ?: return 0
        
        if (!license.isExpired()) return 0
        
        val graceEnd = license.expiresAt + (3 * 86400000L)
        val remaining = graceEnd - System.currentTimeMillis()
        
        return if (remaining > 0) {
            (remaining / 86400000).toInt()
        } else {
            0
        }
    }

    // CORREGIDO: Acceso thread-safe
    override fun getCachedLicenseInfo(): LicenseInfo? = cachedLicense
    
    // CORREGIDO: Usar mutex para evitar race condition con validate()
    override suspend fun clearCache() {
        cacheMutex.withLock {
            cachedLicense = null
        }
    }

    private fun parseLicense(json: String): LicenseInfo {
        val obj = gson.fromJson(json, Map::class.java)
        return LicenseInfo(
            key = obj["key"] as? String ?: "",
            tier = SubscriptionTier.fromName(obj["tier"] as? String ?: "FREE"),
            expiresAt = (obj["expiresAt"] as? Number)?.toLong() ?: 0L,
            isActive = obj["isActive"] as? Boolean ?: false
        )
    }
}
