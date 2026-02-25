/*
 * SponsorFlow Nexus v1.0 - Download Types
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
) {
    init {
        require(totalBytes >= 0) { "Total bytes must be non-negative" }
        require(bytesDownloaded >= 0) { "Downloaded bytes must be non-negative" }
        require(bytesDownloaded <= totalBytes) { "Downloaded bytes cannot exceed total bytes" }
        require(percentage in 0..100) { "Percentage must be between 0 and 100" }
    }
}
