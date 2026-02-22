/*
 * SponsorFlow Nexus - Admin Types
 */
package com.sponsorflow.nexus.admin

data class AdminHeartbeat(
    val deviceId: String,
    val appVersion: String = "2.4.0",
    val lastUserAction: String = "app_start",
    val modelDownloaded: Boolean = false,
    val networkType: String = "unknown",
    val batteryLevel: Int = 0
)

data class AdminCommand(
    val type: String,
    val payload: String? = null
)

data class AdminWebhooks(
    val heartbeatUrl: String,
    val errorUrl: String
)

data class CommandResult(
    val success: Boolean,
    val message: String
)