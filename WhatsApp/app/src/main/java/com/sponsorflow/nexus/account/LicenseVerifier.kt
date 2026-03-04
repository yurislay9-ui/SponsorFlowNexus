/*
 * SponsorFlow Nexus v1.0 - License Verifier
 * Uses NexusConfigManager as BRAIN - URLs hidden from UI
 */
package com.sponsorflow.nexus.account

import android.content.Context
import com.sponsorflow.nexus.config.NexusConfigManager
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
    private val sessionManager: SessionManager
) : ILicenseValidator {

    // BRAIN - gets URL from GitHub config
    private val configManager = NexusConfigManager(context)
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val cacheMutex = Mutex()
    private var cachedLicense: LicenseInfo? = null

    // Get server URL from BRAIN (GitHub) - never exposed in UI
    private fun getServerUrl(): String = configManager.getApiBaseUrl()

    private fun getAppVersion(): String {
        return try {
            com.sponsorflow.nexus.BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "2.4.0"
        }
    }

    override suspend fun validate(licenseKey: String): AppResult<LicenseInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val serverUrl = getServerUrl()
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

    override fun getCachedLicenseInfo(): LicenseInfo? = cachedLicense
    
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
