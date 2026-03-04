/*
 * Voice Constants - Providers and settings
 */
package com.sponsorflow.nexus.voice

/**
 * Constantes del sistema de voz
 */
object VoiceConstants {

    // Proveedores de STT (Speech-to-Text)
    val STT_PROVIDERS = listOf(
        "android" to "Android Speech Recognition (offline)",
        "google" to "Google Speech API (online)",
        "whisper" to "Whisper API (online)"
    )

    // Proveedores de TTS (Text-to-Speech)
    val TTS_PROVIDERS = listOf(
        "device" to "TTS del dispositivo",
        "outetts" to "OuteTTS (neural)",
        "piper" to "Piper TTS (offline)"
    )

    // Anti-detección config
    const val MIN_DELAY_BETWEEN_VOICE_MS = 5000L // 5 segundos mínimo
    const val MAX_VOICE_MESSAGES_PER_HOUR = 10

    // SharedPreferences keys
    const val PREF_VOICE = "nexus_voice"
    const val PREF_TTS_PROVIDER = "tts_provider"
    const val PREF_STT_PROVIDER = "stt_provider"
    const val PREF_DEFAULT_LANGUAGE = "default_language"
    const val PREF_VOICE_CLONE_MAX_SECONDS = "voice_clone_max_seconds"

    // Defaults
    const val DEFAULT_TTS_PROVIDER = "device"
    const val DEFAULT_STT_PROVIDER = "android"
    const val DEFAULT_LANGUAGE = "es"
    const val DEFAULT_VOICE_CLONE_MAX_SECONDS = 12
}
