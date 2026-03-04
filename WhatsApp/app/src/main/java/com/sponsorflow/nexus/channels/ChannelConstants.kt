/*
 * Multi-Channel Constants
 */
package com.sponsorflow.nexus.channels

object ChannelConstants {
    const val PREF_CHANNELS = "channel_configs"
    const val MAX_MESSAGES_PER_DAY_PER_CHANNEL = 500
    const val MAX_TOTAL_CHANNELS = 5
    
    val DEFAULT_PACKAGES = mapOf(
        Channel.WHATSAPP to ChannelPackage(Channel.WHATSAPP, "com.whatsapp", "com.whatsapp.Main", "com.whatsapp:id/entry"),
        Channel.MESSENGER to ChannelPackage(Channel.MESSENGER, "com.facebook.orca", "com.facebook.messaging.MainActivity", "com.facebook.orca:idcomposer_input"),
        Channel.INSTAGRAM to ChannelPackage(Channel.INSTAGRAM, "com.instagram.android", "com.instagram.android.MainActivity", "com.instagram.android:id_row_text_input"),
        Channel.TELEGRAM to ChannelPackage(Channel.TELEGRAM, "org.telegram.messenger", "org.telegram.uiLaunchActivity", "org.telegram.ui.Components.Components.EditTextCursor"),
        Channel.DISCORD to ChannelPackage(Channel.DISCORD, "com.discord", "com.discord.main.MainActivity", "com.discord.widget.app.WidgetEditText")
    )
}
