/*
 * Voice Models - Data classes and enums
 */
package com.sponsorflow.nexus.voice

/**
 * Configuración de voz
 */
data class VoiceConfig(
    val maxDurationSeconds: Int = 12,
    val sampleRate: Int = 16000,
    val encoding: Int = 16,
    val channels: Int = 1,
    val format: String = "m4a"
)

/**
 * Voz clonada de un cliente
 */
data class VoiceClone(
    val id: String,
    val phone: String,
    val audioPath: String,
    val voiceProfile: ByteArray,
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VoiceClone
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Mensaje de voz
 */
data class VoiceMessage(
    val id: String,
    val phone: String,
    val audioPath: String,
    val durationSeconds: Int,
    val transcribedText: String?,
    val responseAudioPath: String?,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Funciones de voz disponibles
 */
enum class VoiceFeature {
    TEXT_TO_SPEECH,  // IA responde con voz
    SPEECH_TO_TEXT,  // Transcribe nota de voz del cliente
    VOICE_CLONE      // Clona voz del cliente
}

/**
 * Resultado del procesamiento de voz
 */
data class VoiceProcessingResult(
    val success: Boolean,
    val transcribedText: String? = null,
    val responseAudioPath: String? = null,
    val usedVoiceClone: Boolean = false,
    val error: String? = null
)

/**
 * Estado anti-detección de voz
 */
data class VoiceAntiDetectionStatus(
    val messagesThisHour: Int,
    val maxPerHour: Int,
    val lastMessageTime: Long
)

/**
 * Estadísticas de voz
 */
data class VoiceStats(
    val totalVoiceMessages: Int,
    val voiceMessagesThisHour: Int,
    val totalClones: Int,
    val lastVoiceTime: Long
)
