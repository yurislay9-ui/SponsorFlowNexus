/*
 * Window Models
 */
package com.sponsorflow.nexus.window

data class ChatWindow(
    val phoneNumber: String,
    val lastMessageTime: Long,
    val windowStartTime: Long,
    val windowEndTime: Long,
    val messagesInWindow: Int = 0,
    val isActive: Boolean = true,
    val startedByCustomer: Boolean = false
)

enum class WindowStatus { ACTIVE, EXPIRED, NEAR_EXPIRE, NO_WINDOW }

data class WindowCheckResult(
    val canRespond: Boolean,
    val status: WindowStatus,
    val remainingTimeMs: Long = 0,
    val messagesSent: Int = 0,
    val reason: String? = null
)

data class WindowConfig(
    val windowDurationHours: Int = 24,
    val warningBeforeExpireMinutes: Int = 60,
    val maxMessagesPerWindow: Int = 28,
    val allowExpiredResponse: Boolean = false,
    val enabled: Boolean = true
)

object WindowConstants {
    const val HOURS_24_MS = 24 * 60 * 60 * 1000L
    const val HOUR_MS = 60 * 60 * 1000L
}
