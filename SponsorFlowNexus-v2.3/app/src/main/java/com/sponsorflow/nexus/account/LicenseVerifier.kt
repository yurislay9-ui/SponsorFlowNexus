/*
 * SponsorFlow Nexus v2.3 - License Verifier
 */
package com.sponsorflow.nexus.account

import android.content.Context
import com.sponsorflow.nexus.core.contracts.security.ILicenseValidator
import com.sponsorflow.nexus.core.contracts.security.LicenseInfo
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import com.sponsorflow.nexus.core.result.AppError
import com.sponsorflow.nexus.core.result.AppResult
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.google.gson.Gson

class LicenseVerifier(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val serverUrl: String
) : ILicenseValidator {

    private val client = OkHttpClient.Builder().build()
    private val gson = Gson()
    private var cachedLicense: LicenseInfo? = null

    override suspend fun validate(licenseKey: String): AppResult<LicenseInfo> {
        return try {
            val deviceId = sessionManager.getDeviceId()
            val body = gson.toJson(mapOf(
                "licenseKey" to licenseKey,
                "deviceId" to deviceId,
                "appVersion" to "2.3.0"
            ))
            val request = Request.Builder()
                .url("$serverUrl/api/license/validate")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val info = parseLicense(response.body?.string() ?: "")
                cachedLicense = info
                AppResult.Success(info)
            } else {
                AppResult.Error(AppError.LicenseError("Licencia inválida"))
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.fromException(e))
        }
    }

    override suspend fun refresh(): AppResult<LicenseInfo> {
        return try {
            val key = cachedLicense?.key ?: return AppResult.Error(AppError.LicenseError("No hay licencia"))
            validate(key)
        } catch (e: Exception) {
            AppResult.Error(AppError.fromException(e))
        }
    }

    override fun isGracePeriodActive(): Boolean {
        val license = cachedLicense ?: return false
        return license.isExpired() && getRemainingGraceDays() > 0
    }

    override fun getRemainingGraceDays(): Int = cachedLicense?.let {
        if (!it.isExpired()) return 0
        val graceEnd = it.expiresAt + (3 * 86400000L)
        val remaining = graceEnd - System.currentTimeMillis()
        return if (remaining > 0) (remaining / 86400000).toInt() else 0
    } ?: 0

    override fun getCachedLicenseInfo(): LicenseInfo? = cachedLicense
    override fun clearCache() { cachedLicense = null }

    private fun parseLicense(json: String): LicenseInfo {
        val obj = gson.fromJson(json, Map::class.java)
        return LicenseInfo(
            key = obj["key"] as? String ?: "",
            tier = SubscriptionTier.fromName(obj["tier"] as? String ?: "FREE"),
            expiresAt = (obj["expiresAt"] as? Number)?.toLong() ?: 0L,
            isActive = obj["isActive"] as? Boolean ?: false
        )
    }

    companion object {
        private const val SERVER_URL = "https://api.sponsorflow.com"
    }
}