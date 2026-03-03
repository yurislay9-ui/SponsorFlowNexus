/*
 * SponsorFlow Nexus - Multi-Channel Manager
 * WhatsApp, Messenger, Instagram, Telegram, Discord
 * Usa el mismo sistema: Notificaciones + Accesibilidad
 * SOLO VIP
 */
package com.sponsorflow.nexus.channels

import android.content.Context
import android.content.SharedPreferences
import android.accessibleservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap

enum class Channel(
    val displayName: String, 
    val icon: String,
    val packageName: String,
    val notificationListener: String
) {
    WHATSAPP(
        "WhatsApp", "💬", 
        "com.whatsapp",
        "com.whatsapp"
    ),
    MESSENGER(
        "Messenger", "📱",
        "com.facebook.orca",
        "com.facebook.orca"
    ),
    INSTAGRAM(
        "Instagram", "📸",
        "com.instagram.android",
        "com.instagram.android"
    ),
    TELEGRAM(
        "Telegram", "✈️",
        "org.telegram.messenger",
        "org.telegram.messenger"
    ),
    DISCORD(
        "Discord", "🎮",
        "com.discord",
        "com.discord"
    )
}

data class ChannelConfig(
    val channel: Channel,
    val isEnabled: Boolean = false,
    val isNotificationAccessEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isDefault: Boolean = false
)

data class ChannelMessage(
    val id: String,
    val channel: Channel,
    val senderId: String,
    val senderName: String?,
    val message: String,
    val timestamp: Long,
    val isOutgoing: Boolean,
    val attachments: List<String>? = null,
    val replyTo: String? = null
)

data class ChannelConversation(
    val channel: Channel,
    val contactId: String,
    val contactName: String?,
    val lastMessage: String?,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isTyping: Boolean = false
)

data class ChannelPackage(
    val channel: Channel,
    val packageName: String,
    val activityToOpen: String? = null,
    val inputFieldId: String? = null,
    val sendButtonId: String? = null
)

class MultiChannelManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_channels", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Configuraciones de canales
    private val channelConfigs = ConcurrentHashMap<Channel, ChannelConfig>()

    // Conversaciones activas por canal
    private val conversations = ConcurrentHashMap<Channel, MutableMap<String, ChannelConversation>>()

    // Mapping de paquetes
    private val channelPackages = mapOf(
        Channel.WHATSAPP to ChannelPackage(
            Channel.WHATSAPP, 
            "com.whatsapp",
            "com.whatsapp.Main",
            "com.whatsapp:id/entry"
        ),
        Channel.MESSENGER to ChannelPackage(
            Channel.MESSENGER,
            "com.facebook.orca",
            "com.facebook.messaging.MainActivity",
            "com.facebook.orca:idcomposer_input"
        ),
        Channel.INSTAGRAM to ChannelPackage(
            Channel.INSTAGRAM,
            "com.instagram.android",
            "com.instagram.android.MainActivity",
            "com.instagram.android:id_row_text_input"
        ),
        Channel.TELEGRAM to ChannelPackage(
            Channel.TELEGRAM,
            "org.telegram.messenger",
            "org.telegram.uiLaunchActivity",
            "org.telegram.ui.Components.Components.EditTextCursor"
        ),
        Channel.DISCORD to ChannelPackage(
            Channel.DISCORD,
            "com.discord",
            "com.discord.main.MainActivity",
            "com.discord.widget.app.WidgetEditText"
        )
    )

    companion object {
        private const val PREF_CHANNELS = "channel_configs"

        // Límites por canal
        const val MAX_MESSAGES_PER_DAY_PER_CHANNEL = 500
        const val MAX_TOTAL_CHANNELS = 5
    }

    // ==================== CONFIGURACIÓN DE CANALES ====================

    /**
     * Configurar un canal
     */
    fun configureChannel(config: ChannelConfig): Boolean {
        val currentEnabled = channelConfigs.values.count { it.isEnabled }
        if (config.isEnabled && currentEnabled >= MAX_TOTAL_CHANNELS && !channelConfigs[config.channel]!!.isEnabled) {
            return false
        }

        channelConfigs[config.channel] = config
        saveConfigs()
        return true
    }

    /**
     * Obtener configuración de canal
     */
    fun getChannelConfig(channel: Channel): ChannelConfig? = channelConfigs[channel]

    /**
     * Obtener todos los canales configurados
     */
    fun getConfiguredChannels(): List<ChannelConfig> = channelConfigs.values.filter { it.isEnabled }

    /**
     * Obtener canal por defecto
     */
    fun getDefaultChannel(): ChannelConfig? =
        channelConfigs.values.find { it.isDefault && it.isEnabled }
            ?: channelConfigs.values.firstOrNull { it.isEnabled }

    /**
     * Habilitar canal (notificaciones + accesibilidad)
     */
    fun enableChannel(channel: Channel): Boolean {
        val config = channelConfigs[channel] ?: ChannelConfig(channel = channel)
        return configureChannel(config.copy(isEnabled = true))
    }

    /**
     * Deshabilitar canal
     */
    fun disableChannel(channel: Channel) {
        channelConfigs[channel]?.let { config ->
            channelConfigs[channel] = config.copy(isEnabled = false)
            saveConfigs()
        }
    }

    /**
     * Establecer canal por defecto
     */
    fun setDefaultChannel(channel: Channel) {
        channelConfigs.values.forEach { config ->
            if (config.isDefault) {
                channelConfigs[config.channel] = config.copy(isDefault = false)
            }
        }

        channelConfigs[channel]?.let { config ->
            channelConfigs[channel] = config.copy(isDefault = true)
        }
        saveConfigs()
    }

    /**
     * Marcar que tiene acceso a notificaciones
     */
    fun setNotificationAccessEnabled(channel: Channel, enabled: Boolean) {
        channelConfigs[channel]?.let { config ->
            channelConfigs[channel] = config.copy(isNotificationAccessEnabled = enabled)
            saveConfigs()
        }
    }

    /**
     * Marcar que tiene accesibilidad habilitada
     */
    fun setAccessibilityEnabled(channel: Channel, enabled: Boolean) {
        channelConfigs[channel]?.let { config ->
            channelConfigs[channel] = config.copy(isAccessibilityEnabled = enabled)
            saveConfigs()
        }
    }

    /**
     * Obtener paquetes de canal
     */
    fun getChannelPackage(channel: Channel): ChannelPackage? = channelPackages[channel]

    /**
     * Obtener lista de paquetes instalados
     */
    fun getInstalledChannels(): List<Channel> {
        val installed = mutableListOf<Channel>()
        
        for (channel in Channel.entries) {
            try {
                context.packageManager.getPackageInfo(channel.packageName, 0)
                installed.add(channel)
            } catch (e: Exception) {
                // Package no está instalado
            }
        }
        
        return installed
    }

    // ==================== PROCESAR MENSAJES ====================

    /**
     * Procesar notificación entrante (desde NotificationListener)
     */
    fun processNotification(
        channel: Channel,
        senderId: String,
        senderName: String?,
        message: String,
        timestamp: Long
    ): ChannelMessage {
        val messageId = "MSG_${channel.name}_${System.currentTimeMillis()}"

        val channelMessage = ChannelMessage(
            id = messageId,
            channel = channel,
            senderId = senderId,
            senderName = senderName,
            message = message,
            timestamp = timestamp,
            isOutgoing = false,
            attachments = null
        )

        // Guardar en conversación
        updateConversation(channel, senderId, senderName, message)

        return channelMessage
    }

    /**
     * Procesar evento de accesibilidad (nuevo mensaje)
     */
    fun processAccessibilityEvent(
        channel: Channel,
        eventType: Int,
        senderId: String,
        senderName: String?,
        message: String
    ): ChannelMessage? {
        // Solo procesar eventos de texto
        if (eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            return null
        }

        return processNotification(
            channel = channel,
            senderId = senderId,
            senderName = senderName,
            message = message,
            timestamp = System.currentTimeMillis()
        )
    }

    // ==================== ENVIAR MENSAJES ====================

    /**
     * Enviar mensaje por canal específico
     * Usa AccessibilityService para escribir y enviar
     */
    fun sendMessage(
        channel: Channel,
        recipientId: String,
        message: String,
        attachments: List<String>? = null
    ): String? {
        val config = channelConfigs[channel] ?: return null
        if (!config.isEnabled) return null
        if (!config.isAccessibilityEnabled) return null

        // Verificar límites
        if (!canSendMessage(channel)) return null

        val messageId = "MSG_${channel.name}_${System.currentTimeMillis()}"

        // Simular envío (en implementación real, usar AccessibilityService)
        val success = simulateSendMessage(channel, recipientId, message)

        if (success) {
            incrementMessageCount(channel)
            return messageId
        }

        return null
    }

    /**
     * Simular envío de mensaje
     * Placeholder para AccessibilityService
     */
    private fun simulateSendMessage(channel: Channel, recipient: String, message: String): Boolean {
        // En implementación real:
        // 1. Abrir la app del canal
        // 2. Buscar el contacto
        // 3. Escribir en el campo de texto
        // 4. Presionar el botón de enviar
        
        // Por ahora, retornar true para simular
        return true
    }

    /**
     * Abrir chat en canal
     */
    fun openChat(channel: Channel, contactId: String): Intent? {
        val pkg = channelPackages[channel] ?: return null

        val intent = context.packageManager.getLaunchIntentForPackage(pkg.packageName)
        return intent?.apply {
            putExtra("contact_id", contactId)
        }
    }

    // ==================== CONVERSACIONES ====================

    /**
     * Actualizar conversación
     */
    private fun updateConversation(channel: Channel, contactId: String, contactName: String?, lastMessage: String) {
        val channelConvos = conversations.getOrPut(channel) { ConcurrentHashMap() }

        channelConvos[contactId] = ChannelConversation(
            channel = channel,
            contactId = contactId,
            contactName = contactName,
            lastMessage = lastMessage,
            lastMessageTime = System.currentTimeMillis(),
            unreadCount = (channelConvos[contactId]?.unreadCount ?: 0) + 1
        )
    }

    /**
     * Obtener conversaciones de un canal
     */
    fun getConversations(channel: Channel): List<ChannelConversation> {
        return conversations[channel]?.values?.toList()?.sortedByDescending { it.lastMessageTime } ?: emptyList()
    }

    /**
     * Obtener todas las conversaciones
     */
    fun getAllConversations(): List<ChannelConversation> {
        return conversations.values.flatMap { it.values }
            .sortedByDescending { it.lastMessageTime }
    }

    /**
     * Marcar como leída
     */
    fun markAsRead(channel: Channel, contactId: String) {
        conversations[channel]?.get(contactId)?.let { convo ->
            conversations[channel]?.set(contactId, convo.copy(unreadCount = 0))
        }
    }

    // ==================== ANTI-DETECCIÓN ====================

    /**
     * Verificar si puede enviar mensaje
     */
    fun canSendMessage(channel: Channel): Boolean {
        val today = "day_${System.currentTimeMillis() / 86400000}"
        val count = prefs.getInt("msg_count_${channel.name}_$today", 0)
        return count < MAX_MESSAGES_PER_DAY_PER_CHANNEL
    }

    /**
     * Incrementar contador de mensajes
     */
    private fun incrementMessageCount(channel: Channel) {
        val today = "day_${System.currentTimeMillis() / 86400000}"
        val key = "msg_count_${channel.name}_$today"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    /**
     * Obtener mensajes restantes hoy
     */
    fun getRemainingMessages(channel: Channel): Int {
        val today = "day_${System.currentTimeMillis() / 86400000}"
        val count = prefs.getInt("msg_count_${channel.name}_$today", 0)
        return maxOf(0, MAX_MESSAGES_PER_DAY_PER_CHANNEL - count)
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtener estadísticas de canales
     */
    fun getChannelStats(): ChannelStats {
        val enabledChannels = channelConfigs.values.filter { it.isEnabled }

        val messagesByChannel = enabledChannels.associate { config ->
            val today = "day_${System.currentTimeMillis() / 86400000}"
            val count = prefs.getInt("msg_count_${config.channel.name}_$today", 0)
            config.channel to count
        }

        val totalConversations = conversations.values.sumOf { it.size }

        return ChannelStats(
            configuredChannels = enabledChannels.map { it.channel },
            messagesToday = messagesByChannel,
            totalConversations = totalConversations,
            maxPerChannel = MAX_MESSAGES_PER_DAY_PER_CHANNEL
        )
    }

    // ==================== PERSISTENCIA ====================

    private fun saveConfigs() {
        val json = gson.toJson(channelConfigs)
        prefs.edit().putString(PREF_CHANNELS, json).apply()
    }

    fun loadFromPrefs() {
        val json = prefs.getString(PREF_CHANNELS, null)
        if (json != null) {
            val loaded: Map<Channel, ChannelConfig> = gson.fromJson(json)
            channelConfigs.putAll(loaded)
        }
    }
}

data class ChannelStats(
    val configuredChannels: List<Channel>,
    val messagesToday: Map<Channel, Int>,
    val totalConversations: Int,
    val maxPerChannel: Int
)
