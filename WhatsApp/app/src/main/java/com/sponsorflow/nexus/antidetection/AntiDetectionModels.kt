/*
 * Anti-Detection Models
 */
package com.sponsorflow.nexus.antidetection

/**
 * Configuración anti-detección
 */
data class AntiDetectionConfig(
    val minDelayMs: Long = 5000L,
    val maxDelayMs: Long = 15000L,
    val cutoffHours: Int = 24,
    val maxDailyResponses: Int = 100,
    val maxResponsesPerHour: Int = 20,
    val allowColdMessaging: Boolean = false,
    val enabled: Boolean = true,
    val maxResponseLength: Int = 500,
    val filterBotPhrases: Boolean = true,
    val botPhrases: List<String> = defaultBotPhrases
)

val defaultBotPhrases = listOf(
    "Como modelo de lenguaje", "Como modelo de IA", "Soy un asistente de IA",
    "I am an AI", "I am a language model", "Based on my training",
    "Mi entrenamiento", "Fue entrenado para", "Puedo ayudarte con",
    "Estoy diseñado para", "Tengo la capacidad de", "Gracias por tu pregunta",
    "Espero que esta respuesta", "Si tienes más preguntas", "No puedo garantizar",
    "Es importante notar que", "Ten en cuenta que", "Debo informarte que",
    "Cabe destacar que", "En términos generales", "De manera general",
    "Como sugerencia", "Te recomiendo que", " es importante que",
    "Considera el hecho de"
)

/**
 * Resultado de verificación anti-detección
 */
data class AntiDetectionResult(
    val allowed: Boolean,
    val delayMs: Long = 0,
    val reason: String? = null,
    val shouldAlertOwner: Boolean = false,
    val isPeakHour: Boolean = false
)

/**
 * Uso diario por número
 */
data class DailyUsage(
    val date: String = java.util.Calendar.getInstance().let {
        "${it.get(it.YEAR)}-${it.get(it.MONTH) + 1}-${it.get(it.DAY_OF_MONTH)}"
    },
    var todayCount: Int = 0,
    val hourlyCount: MutableMap<Int, Int> = mutableMapOf()
)

/**
 * Registro de respuesta
 */
data class ResponseRecord(
    val phone: String,
    val timestamp: Long,
    val responseLength: Int
)

/**
 * Estadísticas anti-detección
 */
data class AntiDetectionStats(
    val todayResponses: Int,
    val hourlyResponses: Int,
    val lastActivityTime: Long? = null,
    val is24HourCutoffActive: Boolean = false,
    val dailyLimit: Int,
    val hourlyLimit: Int
)

// Extensiones
fun Char.isEmoji(): Boolean = Character.UnicodeBlock.of(this) == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS ||
    Character.UnicodeBlock.of(this) == Character.UnicodeBlock.EMOTICONS ||
    Character.UnicodeBlock.of(this) == Character.UnicodeBlock.SYMBOLS_AND_PICTURES_EXTENDED_A ||
    this.code in 0x1F600..0x1F64F || this.code in 0x1F300..0x1F5FF ||
    this.code in 0x1F680..0x1F6FF || this.code in 0x1F1E0..0x1F1FF

fun String.endsWithAny(vararg chars: Char): Boolean = if (isEmpty()) false else chars.any { this[length - 1] == it }
