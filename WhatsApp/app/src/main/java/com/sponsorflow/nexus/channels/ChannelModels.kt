/*
 * Multi-Channel Models
 */
package com.sponsorflow.nexus.channels

enum class Channel(
    val displayName: String, 
    val icon: String,
    val packageName: String,
    val notificationListener: String
) {
    WHATSAPP("WhatsApp", "💬", "com.whatsapp", "com.whatsapp"),
    MESSENGER("Messenger", "📱", "com.facebook.orca", "com.facebook.orca"),
    INSTAGRAM("Instagram", "📸", "com.instagram.android", "com.instagram.android"),
    TELEGRAM("Telegram", "✈️", "org.telegram.messenger", "org.telegram.messenger"),
    DISCORD("Discord", "🎮", "com.discord", "com.discord")
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
