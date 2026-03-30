package com.sponsorflow.nexus.admin

import android.content.Context

class AdminControlManager(private val context: Context) {
    fun isAdmin(): Boolean = false
    fun getStats(): Map<String, Any> = emptyMap()

    // NUEVO: usado por HeartbeatWorker
    fun sendHeartbeat(): Boolean = true

    // NUEVO: usado por NexusCrashHandler
    @Suppress("UNUSED_PARAMETER")
    suspend fun reportError(
        errorType: String,
        message: String,
        stackTrace: String
    ): Boolean = true
}
