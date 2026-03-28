/*
 * Smart Queue Manager (Compact)
 */
package com.sponsorflow.nexus.queue

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class SmartQueueManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_queue", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val queues = ConcurrentHashMap<String, MutableList<QueuedMessage>>()
    private var config = QueueConfig()

    fun enqueue(phone: String, message: String, priority: MessagePriority = MessagePriority.NORMAL): QueueResult {
        val queue = queues.getOrPut(phone) { mutableListOf() }
        val msg = QueuedMessage(
            recipientNumber = phone,
            content = message,
            priority = priority
        )
        queue.add(msg)
        return QueueResult(
            success = true,
            messageId = msg.id,
            queueSize = queue.size
        )
    }

    fun dequeue(phone: String): ProcessResult {
        val queue = queues[phone] ?: return ProcessResult(success = false)
        if (queue.isEmpty()) return ProcessResult(success = false)
        val msg = queue.removeAt(0)
        return ProcessResult(
            success = true,
            messageId = msg.id
        )
    }

    fun getQueueSize(phone: String): Int = queues[phone]?.size ?: 0
    fun clearQueue(phone: String) { queues.remove(phone) }
    fun setConfig(newConfig: QueueConfig) { config = newConfig }
    fun getConfig(): QueueConfig = config
    fun loadFromPrefs() { /* Load from prefs */ }
    fun saveToPrefs() { /* Save to prefs */ }
}
