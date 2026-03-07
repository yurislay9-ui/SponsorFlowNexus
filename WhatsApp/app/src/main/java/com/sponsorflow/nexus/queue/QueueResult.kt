package com.sponsorflow.nexus.queue

data class QueueResult(
    val success: Boolean,
    val messageId: String? = null,
    val queueSize: Int = 0,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)