/*
 * Scheduled Messages Manager (Compact)
 */
package com.sponsorflow.nexus.scheduler

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

class ScheduledMessagesManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_scheduler", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val scheduledMessages = ConcurrentHashMap<String, ScheduledMessage>()

    fun scheduleMessage(phone: String, message: String, triggerTime: Long, type: ScheduledMessageType = ScheduledMessageType.CUSTOM): ScheduledMessage {
        val msg = ScheduledMessage("SCH_${System.currentTimeMillis()}", type, "Scheduled", message, phone, null, TriggerType.ABSOLUTE, triggerTime)
        scheduledMessages[msg.id] = msg
        return msg
    }

    fun getScheduledMessages(): List<ScheduledMessage> = scheduledMessages.values.toList()
    fun getPendingMessages(): List<ScheduledMessage> = scheduledMessages.values.filter { it.status == ScheduledMessageStatus.PENDING }.toList()
    fun cancelMessage(messageId: String): Boolean { scheduledMessages.remove(messageId); return true }
    fun markAsSent(messageId: String) { scheduledMessages[messageId]?.let { scheduledMessages[messageId] = it.copy(status = ScheduledMessageStatus.SENT) } }
    fun loadFromPrefs() { /* Load from prefs */ }
}
