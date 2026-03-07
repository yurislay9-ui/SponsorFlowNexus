package com.sponsorflow.nexus.queue

data class ProcessResult(
    val success: Boolean,
    val messageId: String? = null,
    val processedCount: Int = 0,
    val failedCount: Int = 0,
    val skippedCount: Int = 0,
    val errorMessage: String? = null,
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)