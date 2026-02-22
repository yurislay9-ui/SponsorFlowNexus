/*
 * SponsorFlow Nexus v2.3 - Download Types
 */
package com.sponsorflow.nexus.ai.download

sealed class DownloadError {
    object NoSpace : DownloadError()
    object NetworkFailed : DownloadError()
    object CorruptedFile : DownloadError()
    object PermissionDenied : DownloadError()
    data class Unknown(val message: String) : DownloadError()
}

data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Int
)