package com.sponsorflow.nexus.window

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList
import java.util.concurrent.TimeUnit

class TwentyFourHourWindowManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()
    private val messageTimestamps: LinkedList<Long> = LinkedList()

    init {
        loadPersistedTimestamps()
    }

    private fun loadPersistedTimestamps() {
        val serialized = prefs.getString(KEY_TIMESTAMPS, "") ?: ""
        if (serialized.isNotEmpty()) {
            val now = System.currentTimeMillis()
            val windowStart = now - WINDOW_DURATION_MS
            serialized.split(",")
                .mapNotNull { it.toLongOrNull() }
                .filter { it > windowStart }
                .forEach { messageTimestamps.add(it) }
        }
    }

    private fun persistTimestamps() {
        prefs.edit()
            .putString(KEY_TIMESTAMPS, messageTimestamps.joinToString(","))
            .apply()
    }

    suspend fun recordMessage(): Boolean = mutex.withLock {
        cleanExpiredTimestamps()
        if (messageTimestamps.size >= MAX_MESSAGES_PER_DAY) {
            return@withLock false
        }
        messageTimestamps.add(System.currentTimeMillis())
        persistTimestamps()
        true
    }

    suspend fun getMessageCount(): Int = mutex.withLock {
        cleanExpiredTimestamps()
        messageTimestamps.size
    }

    suspend fun getRemainingCapacity(): Int = mutex.withLock {
        cleanExpiredTimestamps()
        (MAX_MESSAGES_PER_DAY - messageTimestamps.size).coerceAtLeast(0)
    }

    suspend fun canSendMessage(): Boolean = mutex.withLock {
        cleanExpiredTimestamps()
        messageTimestamps.size < MAX_MESSAGES_PER_DAY
    }

    suspend fun getOldestMessageTimestamp(): Long? = mutex.withLock {
        cleanExpiredTimestamps()
        messageTimestamps.peekFirst()
    }

    suspend fun getTimeUntilNextSlot(): Long = mutex.withLock {
        cleanExpiredTimestamps()
        if (messageTimestamps.size < MAX_MESSAGES_PER_DAY) return@withLock 0L
        val oldest = messageTimestamps.peekFirst() ?: return@withLock 0L
        val windowStart = System.currentTimeMillis() - WINDOW_DURATION_MS
        (oldest - windowStart).coerceAtLeast(0L)
    }

    suspend fun reset() = mutex.withLock {
        messageTimestamps.clear()
        prefs.edit().remove(KEY_TIMESTAMPS).apply()
    }

    private fun cleanExpiredTimestamps() {
        val windowStart = System.currentTimeMillis() - WINDOW_DURATION_MS
        while (messageTimestamps.isNotEmpty() && (messageTimestamps.peekFirst() ?: Long.MAX_VALUE) <= windowStart) {
            messageTimestamps.pollFirst()
        }
    }

    companion object {
        private const val PREFS_NAME = "twenty_four_hour_window"
        private const val KEY_TIMESTAMPS = "message_timestamps"
        private const val MAX_MESSAGES_PER_DAY = 1000
        private val WINDOW_DURATION_MS = TimeUnit.HOURS.toMillis(24)
    }
}