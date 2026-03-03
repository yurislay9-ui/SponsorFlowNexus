/*
 * SponsorFlow Nexus - Voice Intelligence Manager
 * Audio → Texto → Audio
 * Cumple con anti-detección de WhatsApp
 */
package com.sponsorflow.nexus.voice

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class VoiceConfig(
    val maxDurationSeconds: Int = 12,
    val sampleRate: Int = 16000,
    val encoding: Int = 16,
    val channels: Int = 1,
    val format: String = "m4a"
)

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

data class VoiceMessage(
    val id: String,
    val phone: String,
    val audioPath: String,
    val durationSeconds: Int,
    val transcribedText: String?,
    val responseAudioPath: String?,
    val timestamp: Long = System.currentTimeMillis()
)

enum class VoiceFeature {
    TEXT_TO_SPEECH,  // IA responde con voz
    SPEECH_TO_TEXT,  // Transcribe nota de voz del cliente
    VOICE_CLONE      // Clona voz del cliente
}

class VoiceManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_voice", Context.MODE_PRIVATE)

    // Configuración
    private val config = VoiceConfig()

    // Grabador de audio
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var recordingStartTime: Long = 0

    // Voces clonadas por teléfono
    private val voiceClones = ConcurrentHashMap<String, VoiceClone>()

    // Caché de transcripciones
    private val transcriptionCache = ConcurrentHashMap<String, String>()

    // Estado de mensajes de voz
    private val voiceMessages = ConcurrentHashMap<String, VoiceMessage>()

    companion object {
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
    }

    // ==================== GRABACIÓN ====================

    /**
     * Iniciar grabación de nota de voz
     */
    fun startRecording(phone: String): String? {
        if (isRecording) return null

        val audioDir = File(context.filesDir, "voice")
        if (!audioDir.exists()) audioDir.mkdirs()

        val audioFile = File(audioDir, "voice_${phone}_${System.currentTimeMillis()}.${config.format}")

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(config.sampleRate)
                setAudioEncodingBitRate(config.encoding * 1000)
                setAudioChannels(config.channels)
                setOutputFile(audioFile.absolutePath)
                setMaxDuration(config.maxDurationSeconds * 1000)

                setOnInfoListener { _, what, _ ->
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        stopRecording()
                    }
                }

                prepare()
                start()
            }

            isRecording = true
            recordingStartTime = System.currentTimeMillis()

            return audioFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Detener grabación
     */
    fun stopRecording(): String? {
        if (!isRecording) return null

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val duration = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
            recordingStartTime = 0

            // Retornar la ruta del archivo
            null // Se maneja en el callback
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Cancelar grabación
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignorar
        }
        mediaRecorder = null
        isRecording = false
        recordingStartTime = 0
    }

    /**
     * Verificar si está grabando
     */
    fun isCurrentlyRecording(): Boolean = isRecording

    // ==================== SPEECH TO TEXT ====================

    /**
     * Transcribir audio a texto
     * Usa el reconocimiento de voz de Android
     */
    fun transcribeAudio(audioPath: String, language: String = "es"): String? {
        // Placeholder para transcripción real
        // En producción, usar:
        // - Android SpeechRecognizer
        // - Google Speech API
        // - Whisper API

        // Por ahora, retornamos null para indicar que necesita integración
        // La implementación real dependería del proveedor elegido

        return null
    }

    /**
     * Transcribir usando Android (offline si está disponible)
     */
    fun transcribeWithAndroid(audioPath: String, language: String = "es"): String? {
        // Implementación con SpeechRecognizer de Android
        // Requiere: android.permission.RECORD_AUDIO

        val cached = transcriptionCache[audioPath]
        if (cached != null) return cached

        // Placeholder - en implementación real:
        // 1. Convertir audio al formato requerido
        // 2. Llamar SpeechRecognizer
        // 3. Retornar texto

        return null
    }

    /**
     * Transcribir con Google Speech API
     */
    fun transcribeWithGoogle(audioPath: String, language: String = "es-ES"): String? {
        // Placeholder para Google Cloud Speech-to-Text
        // Requiere API key

        return null
    }

    /**
     * Transcribir con Whisper API
     */
    fun transcribeWithWhisper(audioPath: String, language: String = "es"): String? {
        // Placeholder para Whisper API
        // Más preciso para español

        return null
    }

    // ==================== TEXT TO SPEECH ====================

    /**
     * Convertir texto a voz
     * Retorna la ruta del archivo de audio
     */
    fun textToSpeech(
        text: String,
        phone: String,
        useClone: Boolean = false,
        language: String = "es"
    ): String? {

        val outputDir = File(context.filesDir, "tts")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outputFile = File(outputDir, "tts_${phone}_${System.currentTimeMillis()}.wav")

        // Verificar longitud (anti-detección)
        val estimatedDuration = estimateSpeechDuration(text)
        if (estimatedDuration > config.maxDurationSeconds) {
            // Dividir texto en partes más pequeñas
            val parts = splitTextForTTS(text, config.maxDurationSeconds)
            // Generar cada parte por separado
            // потом combinar
        }

        // Elegir proveedor según configuración
        return when {
            useClone && hasVoiceClone(phone) -> {
                generateWithVoiceClone(text, phone, outputFile.absolutePath)
            }
            else -> {
                generateWithTTS(text, outputFile.absolutePath, language)
            }
        }
    }

    /**
     * Estimar duración del speech
     */
    private fun estimateSpeechDuration(text: String): Int {
        // Aproximación: 150 palabras por minuto = 2.5 palabras por segundo
        val words = text.split(" ").size
        return (words / 2.5).toInt() + 1
    }

    /**
     * Dividir texto en partes para TTS
     */
    private fun splitTextForTTS(text: String, maxSeconds: Int): List<String> {
        val parts = mutableListOf<String>()
        val sentences = text.split(". ", "!", "?")

        var currentPart = StringBuilder()
        var currentDuration = 0

        for (sentence in sentences) {
            val sentenceDuration = estimateSpeechDuration(sentence)

            if (currentDuration + sentenceDuration > maxSeconds && currentPart.isNotEmpty()) {
                parts.add(currentPart.toString().trim())
                currentPart = StringBuilder()
                currentDuration = 0
            }

            currentPart.append(sentence).append(". ")
            currentDuration += sentenceDuration
        }

        if (currentPart.isNotEmpty()) {
            parts.add(currentPart.toString().trim())
        }

        return parts
    }

    /**
     * Generar TTS con OuteTTS
     */
    private fun generateWithTTS(text: String, outputPath: String, language: String): String? {
        // Placeholder para OuteTTS
        // En producción:
        // 1. Cargar modelo OuteTTS
        // 2. Generar audio
        // 3. Guardar en outputPath

        return null
    }

    /**
     * Generar TTS con voz clonada
     */
    private fun generateWithVoiceClone(text: String, phone: String, outputPath: String): String? {
        val clone = voiceClones[phone] ?: return generateWithTTS(text, outputPath, "es")

        // Placeholder para voice cloning con OuteTTS
        // Usar el perfil de voz del cliente para generar

        return null
    }

    // ==================== VOICE CLONING ====================

    /**
     * Clonar voz del cliente
     * Recibe un audio de referencia (máx 12 segundos)
     */
    fun cloneVoice(phone: String, audioPath: String): Boolean {
        // Verificar duración del audio
        val audioFile = File(audioPath)
        if (!audioFile.exists()) return false

        // Verificar que no exceda el límite
        val maxDuration = prefs.getInt("voice_clone_max_seconds", 12)

        // En producción:
        // 1. Enviar audio a API de voice cloning (OuteTTS, Coqui, etc)
        // 2. Recibir perfil de voz
        // 3. Guardar para uso futuro

        val cloneId = "clone_${phone}_${System.currentTimeMillis()}"

        voiceClones[phone] = VoiceClone(
            id = cloneId,
            phone = phone,
            audioPath = audioPath,
            voiceProfile = ByteArray(0) // Placeholder
        )

        // Guardar referencia
        prefs.edit()
            .putString("clone_$phone", cloneId)
            .putLong("clone_time_$phone", System.currentTimeMillis())
            .apply()

        return true
    }

    /**
     * Verificar si tiene voz clonada
     */
    fun hasVoiceClone(phone: String): Boolean {
        return voiceClones.containsKey(phone)
    }

    /**
     * Obtener voz clonada
     */
    fun getVoiceClone(phone: String): VoiceClone? = voiceClones[phone]

    /**
     * Eliminar voz clonada
     */
    fun deleteVoiceClone(phone: String): Boolean {
        val clone = voiceClones.remove(phone) ?: return false

        // Eliminar archivo
        try {
            File(clone.audioPath).delete()
        } catch (e: Exception) {
            // Ignorar
        }

        prefs.edit().remove("clone_$phone").apply()
        return true
    }

    // ==================== ANTI-DETECCIÓN ====================

    /**
     * Verificar si puede enviar mensaje de voz
     * Respeta límites anti-detección
     */
    fun canSendVoiceMessage(phone: String): Pair<Boolean, Long> {
        val now = System.currentTimeMillis()
        val hourKey = "hour_${now / 3600000}"

        // Contador por hora
        val messagesThisHour = prefs.getInt("voice_$hourKey", 0)

        if (messagesThisHour >= MAX_VOICE_MESSAGES_PER_HOUR) {
            return Pair(false, -1) // Límite alcanzado
        }

        // Verificar delay mínimo
        val lastVoiceTime = prefs.getLong("last_voice_$phone", 0)
        val timeSinceLastVoice = now - lastVoiceTime

        if (timeSinceLastVoice < MIN_DELAY_BETWEEN_VOICE_MS) {
            return Pair(false, MIN_DELAY_BETWEEN_VOICE_MS - timeSinceLastVoice)
        }

        return Pair(true, 0)
    }

    /**
     * Registrar mensaje de voz enviado
     */
    fun registerVoiceMessage(phone: String) {
        val now = System.currentTimeMillis()
        val hourKey = "hour_${now / 3600000}"

        prefs.edit()
            .putInt("voice_$hourKey", prefs.getInt("voice_$hourKey", 0) + 1)
            .putLong("last_voice_$phone", now)
            .apply()
    }

    /**
     * Obtener estado anti-detección de voz
     */
    fun getVoiceAntiDetectionStatus(): VoiceAntiDetectionStatus {
        val now = System.currentTimeMillis()
        val hourKey = "hour_${now / 3600000}"

        return VoiceAntiDetectionStatus(
            messagesThisHour = prefs.getInt("voice_$hourKey", 0),
            maxPerHour = MAX_VOICE_MESSAGES_PER_HOUR,
            lastMessageTime = prefs.getLong("last_voice_global", 0)
        )
    }

    // ==================== CONFIGURACIÓN ====================

    /**
     * Configurar proveedor de TTS
     */
    fun setTTSProvider(provider: String) {
        prefs.edit().putString("tts_provider", provider).apply()
    }

    /**
     * Obtener proveedor de TTS
     */
    fun getTTSProvider(): String = prefs.getString("tts_provider", "device") ?: "device"

    /**
     * Configurar proveedor de STT
     */
    fun setSTTProvider(provider: String) {
        prefs.edit().putString("stt_provider", provider).apply()
    }

    /**
     * Obtener proveedor de STT
     */
    fun getSTTProvider(): String = prefs.getString("stt_provider", "android") ?: "android"

    /**
     * Configurar idioma por defecto
     */
    fun setDefaultLanguage(language: String) {
        prefs.edit().putString("default_language", language).apply()
    }

    /**
     * Obtener idioma por defecto
     */
    fun getDefaultLanguage(): String = prefs.getString("default_language", "es") ?: "es"

    // ==================== MENSAJES DE VOZ ====================

    /**
     * Procesar nota de voz del cliente
     * 1. Transcribir
     * 2. Generar respuesta con IA
     * 3. Convertir a voz
     */
    fun processVoiceMessage(
        audioPath: String,
        phone: String,
        language: String = "es",
        generateVoiceResponse: Boolean = true
    ): VoiceProcessingResult {

        // 1. Transcribir audio
        val transcribedText = when (getSTTProvider()) {
            "android" -> transcribeWithAndroid(audioPath, language)
            "google" -> transcribeWithGoogle(audioPath, language)
            "whisper" -> transcribeWithWhisper(audioPath, language)
            else -> transcribeWithAndroid(audioPath, language)
        } ?: return VoiceProcessingResult(
            success = false,
            error = "Transcription failed"
        )

        // 2. Generar respuesta de texto (se hace con AI Engine)
        // Esto se maneja en el flujo principal

        // 3. Opcional: generar respuesta de voz
        var responseAudioPath: String? = null
        if (generateVoiceResponse) {
            responseAudioPath = textToSpeech(
                "Respuesta procesada", // Placeholder - aquí va la respuesta real
                phone,
                useClone = hasVoiceClone(phone)
            )
        }

        return VoiceProcessingResult(
            success = true,
            transcribedText = transcribedText,
            responseAudioPath = responseAudioPath,
            usedVoiceClone = hasVoiceClone(phone)
        )
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Obtener estadísticas de voz
     */
    fun getVoiceStats(): VoiceStats {
        val now = System.currentTimeMillis()
        val hourKey = "hour_${now / 3600000}"

        return VoiceStats(
            totalVoiceMessages = prefs.getInt("total_voice", 0),
            voiceMessagesThisHour = prefs.getInt("voice_$hourKey", 0),
            totalClones = voiceClones.size,
            lastVoiceTime = prefs.getLong("last_voice_global", 0)
        )
    }

    // ==================== PERSISTENCIA ====================

    fun loadFromPrefs() {
        // Cargar voces clonadas
        // En implementación real, cargar los perfiles

        val clonesDir = File(context.filesDir, "voice_clones")
        if (clonesDir.exists()) {
            clonesDir.listFiles()?.forEach { file ->
                val phone = file.nameWithoutExtension.substringAfter("clone_").substringBefore("_")
                if (phone.isNotEmpty()) {
                    voiceClones[phone] = VoiceClone(
                        id = file.nameWithoutExtension,
                        phone = phone,
                        audioPath = file.absolutePath,
                        voiceProfile = ByteArray(0)
                    )
                }
            }
        }
    }
}

data class VoiceProcessingResult(
    val success: Boolean,
    val transcribedText: String? = null,
    val responseAudioPath: String? = null,
    val usedVoiceClone: Boolean = false,
    val error: String? = null
)

data class VoiceAntiDetectionStatus(
    val messagesThisHour: Int,
    val maxPerHour: Int,
    val lastMessageTime: Long
)

data class VoiceStats(
    val totalVoiceMessages: Int,
    val voiceMessagesThisHour: Int,
    val totalClones: Int,
    val lastVoiceTime: Long
)
