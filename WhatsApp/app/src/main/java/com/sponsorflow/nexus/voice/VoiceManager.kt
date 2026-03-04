/*
 * Voice Manager (Compact)
 */
package com.sponsorflow.nexus.voice

import android.content.Context
import android.content.SharedPreferences

class VoiceManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_voice", Context.MODE_PRIVATE)

    fun transcribe(audioPath: String): String? = null // Placeholder
    fun speak(text: String, phone: String): String? = null // Placeholder
    fun cloneVoice(phone: String, audioPath: String): Boolean = true
    fun hasVoiceClone(phone: String): Boolean = prefs.getBoolean("clone_$phone", false)
    fun deleteVoiceClone(phone: String): Boolean { prefs.edit().remove("clone_$phone").apply(); return true }
    fun setTTSProvider(provider: String) { prefs.edit().putString("tts_provider", provider).apply() }
    fun getTTSProvider(): String = prefs.getString("tts_provider", "device") ?: "device"
    fun setSTTProvider(provider: String) { prefs.edit().putString("stt_provider", provider).apply() }
    fun getSTTProvider(): String = prefs.getString("stt_provider", "android") ?: "android"
}
