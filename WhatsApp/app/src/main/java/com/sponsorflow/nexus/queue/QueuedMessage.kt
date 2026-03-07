package com.sponsorflow.nexus.queue

import java.util.UUID

data class QueuedMessage(
    val id: String = UUID.randomUUID().toString(),
    val recipientNumber: String,
    val content: String,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val scheduledTime: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val tag: String? = null,
    val metadata: Map<String, String> = emptyMap()
)