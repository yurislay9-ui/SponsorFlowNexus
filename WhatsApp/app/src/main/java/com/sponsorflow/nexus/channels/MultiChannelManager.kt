/*
 * SponsorFlow Nexus - Multi-Channel Manager
 */
package com.sponsorflow.nexus.channels

import android.content.Context
import android.content.SharedPreferences
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

class MultiChannelManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(ChannelConstants.PREF_CHANNELS, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val channelConfigs = ConcurrentHashMap<Channel, ChannelConfig>()
    private val conversations = ConcurrentHashMap<Channel, MutableMap<String, ChannelConversation>>()

    // ==================== CONFIGURACIÓN ====================

    fun configureChannel(config: ChannelConfig): Boolean {
        val currentEnabled = channelConfigs.values.count { it.isEnabled }
        if (config.isEnabled && currentEnabled >= ChannelConstants.MAX_TOTAL_CHANNELS && !channelConfigs[config.channel]!!.isEnabled) {
            return false
        }
        channelConfigs[config.channel] = config
        saveConfigs()
        return true
    }

    fun getChannelConfig(channel: Channel): ChannelConfig? = channelConfigs[channel]
    fun getConfiguredChannels(): List<ChannelConfig> = channelConfigs.values.filter { it.isEnabled }
    fun getDefaultChannel(): ChannelConfig? = channelConfigs.values.find { it.isDefault && it.isEnabled } ?: channelConfigs.values.firstOrNull { it.isEnabled }
    fun enableChannel(channel: Channel): Boolean = configureChannel(channelConfigs[channel] ?: ChannelConfig(channel = channel).copy(isEnabled = true))
    fun disableChannel(channel: Channel) { channelConfigs[channel]?.let { channelConfigs[channel] = it.copy(isEnabled = false); saveConfigs() } }
    fun setDefaultChannel(channel: Channel) { channelConfigs.values.filter { it.isDefault }.forEach { channelConfigs[it.channel] = it.copy(isDefault = false) }; channelConfigs[channel]?.let { channelConfigs[channel] = it.copy(isDefault = true) }; saveConfigs() }
    fun setNotificationAccessEnabled(channel: Channel, enabled: Boolean) { channelConfigs[channel]?.let { channelConfigs[channel] = it.copy(isNotificationAccessEnabled = enabled); saveConfigs() } }
    fun setAccessibilityEnabled(channel: Channel, enabled: Boolean) { channelConfigs[channel]?.let { channelConfigs[channel] = it.copy(isAccessibilityEnabled = enabled); saveConfigs() } }
    fun getChannelPackage(channel: Channel): ChannelPackage? = ChannelConstants.DEFAULT_PACKAGES[channel]

    fun getInstalledChannels(): List<Channel> = Channel.entries.filter { channel ->
        try { context.packageManager.getPackageInfo(channel.packageName, 0); true } catch (e: Exception) { false }
    }

    // ==================== MENSAJES ====================

    fun processNotification(channel: Channel, senderId: String, senderName: String?, message: String, timestamp: Long): ChannelMessage {
        val msg = ChannelMessage("MSG_${channel.name}_${System.currentTimeMillis()}", channel, senderId, senderName, message, timestamp, false, null, null)
        updateConversation(channel, senderId, senderName, message)
        return msg
    }

    fun processAccessibilityEvent(channel: Channel, eventType: Int, senderId: String, senderName: String?, message: String): ChannelMessage? {
        if (eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return null
        return processNotification(channel, senderId, senderName, message, System.currentTimeMillis())
    }

    fun sendMessage(channel: Channel, recipientId: String, message: String, attachments: List<String>? = null): String? {
        val config = channelConfigs[channel] ?: return null
        if (!config.isEnabled || !config.isAccessibilityEnabled) return null
        if (!canSendMessage(channel)) return null
        val messageId = "MSG_${channel.name}_${System.currentTimeMillis()}"
        incrementMessageCount(channel)
        return messageId
    }

    fun openChat(channel: Channel, contactId: String): Intent? = ChannelConstants.DEFAULT_PACKAGES[channel]?.let {
        context.packageManager.getLaunchIntentForPackage(it.packageName)?.apply { putExtra("contact_id", contactId) }
    }

    // ==================== CONVERSACIONES ====================

    private fun updateConversation(channel: Channel, contactId: String, contactName: String?, lastMessage: String) {
        val channelConvos = conversations.getOrPut(channel) { ConcurrentHashMap() }
        channelConvos[contactId] = ChannelConversation(channel, contactId, contactName, lastMessage, System.currentTimeMillis(), (channelConvos[contactId]?.unreadCount ?: 0) + 1)
    }

    fun getConversations(channel: Channel): List<ChannelConversation> = conversations[channel]?.values?.toList()?.sortedByDescending { it.lastMessageTime } ?: emptyList()
    fun getAllConversations(): List<ChannelConversation> = conversations.values.flatMap { it.values }.sortedByDescending { it.lastMessageTime }
    fun markAsRead(channel: Channel, contactId: String) { conversations[channel]?.get(contactId)?.let { conversations[channel]?.set(contactId, it.copy(unreadCount = 0)) } }

    // ==================== ANTI-DETECCIÓN ====================

    fun canSendMessage(channel: Channel): Boolean {
        val today = "day_${System.currentTimeMillis() / 86400000}"
        return (prefs.getInt("msg_count_${channel.name}_$today", 0)) < ChannelConstants.MAX_MESSAGES_PER_DAY_PER_CHANNEL
    }

    private fun incrementMessageCount(channel: Channel) {
        val today = "day_${System.currentTimeMillis() / 86400000}"
        prefs.edit().putInt("msg_count_${channel.name}_$today", prefs.getInt("msg_count_${channel.name}_$today", 0) + 1).apply()
    }

    fun getRemainingMessages(channel: Channel): Int = maxOf(0, ChannelConstants.MAX_MESSAGES_PER_DAY_PER_CHANNEL - prefs.getInt("msg_count_${channel.name}_day_${System.currentTimeMillis() / 86400000}", 0))

    // ==================== ESTADÍSTICAS ====================

    fun getChannelStats(): ChannelStats {
        val enabled = channelConfigs.values.filter { it.isEnabled }
        val messagesByChannel = enabled.associate { it.channel to prefs.getInt("msg_count_${it.channel.name}_day_${System.currentTimeMillis() / 86400000}", 0) }
        return ChannelStats(enabled.map { it.channel }, messagesByChannel, conversations.values.sumOf { it.size }, ChannelConstants.MAX_MESSAGES_PER_DAY_PER_CHANNEL)
    }

    // ==================== PERSISTENCIA ====================

    private fun saveConfigs() { prefs.edit().putString(ChannelConstants.PREF_CHANNELS, gson.toJson(channelConfigs)).apply() }
    fun loadFromPrefs() { prefs.getString(ChannelConstants.PREF_CHANNELS, null)?.let { channelConfigs.putAll(gson.fromJson(it)) } }
}

data class ChannelStats(
    val configuredChannels: List<Channel>,
    val messagesToday: Map<Channel, Int>,
    val totalConversations: Int,
    val maxPerChannel: Int
)
