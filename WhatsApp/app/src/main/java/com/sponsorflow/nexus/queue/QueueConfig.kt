package com.sponsorflow.nexus.queue

data class QueueConfig(
    val maxQueueSize: Int = 1000,
    val defaultDelayMs: Long = 2000L,
    val minDelayMs: Long = 500L,
    val maxDelayMs: Long = 10000L,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 5000L,
    val batchSize: Int = 10,
    val enablePriorityQueue: Boolean = true,
    val enablePersistence: Boolean = true,
    val processingTimeoutMs: Long = 30000L
)