/*
 * SponsorFlow Nexus v1.0 - License Transfer Types
 * CORREGIDO: Version actualizada a v1.0
 */
package com.sponsorflow.nexus.account

import com.google.gson.annotations.SerializedName

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
