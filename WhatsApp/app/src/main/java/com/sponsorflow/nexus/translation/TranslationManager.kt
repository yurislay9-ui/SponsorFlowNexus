/*
 * SponsorFlow Nexus - Translation Manager
 * Traducción en tiempo real para múltiples idiomas
 * Cumple con anti-detección de WhatsApp
 */
package com.sponsorflow.nexus.translation

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val detectedLanguage: String,
    val targetLanguage: String,
    val confidence: Float,
    val isTranslationNeeded: Boolean
)

data class LanguageConfig(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String,
    val supported: Boolean = true
)

class TranslationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nexus_translation", Context.MODE_PRIVATE)

    // Caché de idiomas detectados por teléfono
    private val languageCache = ConcurrentHashMap<String, String>()

    // Idiomas soportados
    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            LanguageConfig("es", "Español", "Español", "🇪🇸"),
            LanguageConfig("en", "Inglés", "English", "🇺🇸"),
            LanguageConfig("pt", "Portugués", "Português", "🇧🇷"),
            LanguageConfig("fr", "Francés", "Français", "🇫🇷"),
            LanguageConfig("it", "Italiano", "Italiano", "🇮🇹"),
            LanguageConfig("de", "Alemán", "Deutsch", "🇩🇪"),
            LanguageConfig("zh", "Chino", "中文", "🇨🇳"),
            LanguageConfig("ja", "Japonés", "日本語", "🇯🇵"),
            LanguageConfig("ko", "Coreano", "한국어", "🇰🇷"),
            LanguageConfig("ru", "Ruso", "Русский", "🇷🇺"),
            LanguageConfig("ar", "Árabe", "العربية", "🇸🇦"),
            LanguageConfig("hi", "Hindi", "हिन्दी", "🇮🇳")
        )

        // Idiomas Latinomericanos
        val LATAM_LANGUAGES = listOf("es", "pt")

        // Palabras clave para detección de idioma
        private val SPANISH_INDICATORS = listOf(
            "hola", "buenos", "gracias", "por favor", "quiero", "tengo",
            "cuánto", "cuál", "dónde", "cuándo", "cómo", "qué", "sí", "no",
            "buenos días", "buenas tardes", "buenas noches", "está", "tienen",
            "el", "la", "los", "las", "un", "una", "del", "al"
        )

        private val PORTUGUESE_INDICATORS = listOf(
            "olá", "bom", "obrigado", "por favor", "quero", "tenho",
            "quanto", "qual", "onde", "quando", "como", "o que", "sim", "não",
            "bom dia", "boa tarde", "boa noite", "está", "têm",
            "o", "a", "os", "as", "um", "uma", "do", "ao"
        )

        private val ENGLISH_INDICATORS = listOf(
            "hello", "hi", "thanks", "thank you", "please", "want", "have",
            "how", "what", "where", "when", "why", "yes", "no",
            "good morning", "good afternoon", "good evening", "is", "are",
            "the", "a", "an", "of", "to", "in"
        )
    }

    // ==================== DETECCIÓN DE IDIOMA ====================

    /**
     * Detectar idioma del mensaje
     * Retorna código de idioma (es, en, pt, etc.)
     */
    fun detectLanguage(text: String): String {
        val lowerText = text.lowercase()

        // Contar coincidencias con cada idioma
        val spanishScore = SPANISH_INDICATORS.count { lowerText.contains(it) }
        val portugueseScore = PORTUGUESE_INDICATORS.count { lowerText.contains(it) }
        val englishScore = ENGLISH_INDICATORS.count { lowerText.contains(it) }

        // También verificar caracteres específicos
        val hasChineseChars = text.any { it.code in 0x4E00..0x9FFF }
        val hasJapaneseChars = text.any { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }
        val hasArabicChars = text.any { it.code in 0x0600..0x06FF }
        val hasRussianChars = text.any { it.code in 0x0400..0x04FF }
        val hasHindiChars = text.any { it.code in 0x0900..0x097F }
        val hasKoreanChars = text.any { it.code in 0xAC00..0xD7AF || it.code in 0x1100..0x11FF }

        return when {
            hasChineseChars -> "zh"
            hasJapaneseChars -> "ja"
            hasArabicChars -> "ar"
            hasRussianChars -> "ru"
            hasHindiChars -> "hi"
            hasKoreanChars -> "ko"
            spanishScore >= portugueseScore && spanishScore >= englishScore && spanishScore >= 2 -> "es"
            portugueseScore >= spanishScore && portugueseScore >= englishScore && portugueseScore >= 2 -> "pt"
            englishScore >= 2 -> "en"
            spanishScore >= 1 -> "es"
            portugueseScore >= 1 -> "pt"
            englishScore >= 1 -> "en"
            else -> "es" // Default a español para LATAM
        }
    }

    /**
     * Detectar idioma con confianza
     */
    fun detectLanguageWithConfidence(text: String): Pair<String, Float> {
        val lowerText = text.lowercase()

        val spanishScore = SPANISH_INDICATORS.count { lowerText.contains(it) }
        val portugueseScore = PORTUGUESE_INDICATORS.count { lowerText.contains(it) }
        val englishScore = ENGLISH_INDICATORS.count { lowerText.contains(it) }

        val totalScore = spanishScore + portugueseScore + englishScore + 1

        val detected = when {
            spanishScore >= portugueseScore && spanishScore >= englishScore -> "es"
            portugueseScore >= spanishScore && portugueseScore >= englishScore -> "pt"
            englishScore >= spanishScore && englishScore >= portugueseScore -> "en"
            else -> "es"
        }

        val confidence = when (detected) {
            "es" -> spanishScore.toFloat() / totalScore
            "pt" -> portugueseScore.toFloat() / totalScore
            "en" -> englishScore.toFloat() / totalScore
            else -> 0.5f
        }

        return detected to (confidence.coerceIn(0.5f, 1.0f))
    }

    /**
     * Obtener idioma preferido del cliente (desde historial)
     */
    fun getPreferredLanguage(phone: String): String {
        // Verificar caché en memoria
        languageCache[phone]?.let { return it }

        // Verificar prefs
        val saved = prefs.getString("lang_$phone", null)
        if (saved != null) {
            languageCache[phone] = saved
            return saved
        }

        // Default
        return "es"
    }

    /**
     * Guardar idioma preferido del cliente
     */
    fun setPreferredLanguage(phone: String, language: String) {
        languageCache[phone] = language
        prefs.edit().putString("lang_$phone", language).apply()
    }

    // ==================== TRADUCCIÓN ====================

    /**
     * Traducir mensaje del cliente al idioma de la IA
     * (Normalmente español para procesar)
     */
    fun translateToAI(text: String, sourceLanguage: String? = null): TranslationResult {
        val detectedLang = sourceLanguage ?: detectLanguage(text)
        val confidence = detectLanguageWithConfidence(text).second

        // Si ya está en español, no traducir
        if (detectedLang == "es") {
            return TranslationResult(
                originalText = text,
                translatedText = text,
                detectedLanguage = detectedLang,
                targetLanguage = "es",
                confidence = confidence,
                isTranslationNeeded = false
            )
        }

        // Traducir a español
        val translated = translate(text, detectedLang, "es")

        return TranslationResult(
            originalText = text,
            translatedText = translated,
            detectedLanguage = detectedLang,
            targetLanguage = "es",
            confidence = confidence,
            isTranslationNeeded = true
        )
    }

    /**
     * Traducir respuesta de la IA al idioma del cliente
     */
    fun translateToClient(
        aiResponse: String,
        clientLanguage: String,
        preserveFormat: Boolean = true
    ): TranslationResult {
        // Si el cliente habla español, no traducir
        if (clientLanguage == "es") {
            return TranslationResult(
                originalText = aiResponse,
                translatedText = aiResponse,
                detectedLanguage = "es",
                targetLanguage = clientLanguage,
                confidence = 1.0f,
                isTranslationNeeded = false
            )
        }

        // Traducir del español al idioma del cliente
        val translated = translate(aiResponse, "es", clientLanguage)

        return TranslationResult(
            originalText = aiResponse,
            translatedText = translated,
            detectedLanguage = "es",
            targetLanguage = clientLanguage,
            confidence = 0.9f,
            isTranslationNeeded = true
        )
    }

    /**
     * Traducción completa (detectar → procesar → responder)
     */
    fun translateConversation(
        clientMessage: String,
        aiResponse: String,
        phone: String
    ): Pair<String, String> {
        // 1. Detectar idioma del cliente
        val (detectedLang, confidence) = detectLanguageWithConfidence(clientMessage)

        // 2. Guardar idioma preferido
        if (confidence > 0.7f) {
            setPreferredLanguage(phone, detectedLang)
        }

        // 3. Traducir mensaje del cliente al español
        val translatedToAI = translateToAI(clientMessage, detectedLang)

        // 4. Traducir respuesta de la IA al idioma del cliente
        val preferredLang = getPreferredLanguage(phone)
        val translatedToClient = translateToClient(aiResponse, preferredLang)

        return Pair(translatedToAI.translatedText, translatedToClient.translatedText)
    }

    // ==================== MOTOR DE TRADUCCIÓN ====================

    /**
     * Traducir entre dos idiomas
     * NOTA: Para producción, usar APIs como:
     * - Google Translate API
     * - DeepL API
     * - Microsoft Translator
     * - LibreTranslate (self-hosted)
     *
     * Esta implementación es un placeholder con diccionario básico
     */
    private fun translate(text: String, fromLang: String, toLang: String): String {
        // Placeholder: En producción, llamar a API de traducción
        // Por ahora, retornamos el texto original con marca
        // para indicar que necesita traducción externa

        // Dictionary simple para demos
        val dictionary = getSimpleDictionary(fromLang, toLang)

        var result = text
        dictionary.forEach { (word, translation) ->
            result = result.replace(word, translation, ignoreCase = true)
        }

        return result
    }

    /**
     * Diccionario simple para demostración
     * En producción, esto se reemplaza con API
     */
    private fun getSimpleDictionary(fromLang: String, toLang: String): Map<String, String> {
        // Español → Inglés
        if (fromLang == "es" && toLang == "en") {
            return mapOf(
                "hola" to "hello",
                "gracias" to "thanks",
                "por favor" to "please",
                "sí" to "yes",
                "no" to "no",
                "buenos días" to "good morning",
                "buenas tardes" to "good afternoon",
                "buenas noches" to "good night",
                "precio" to "price",
                "comprar" to "buy",
                "producto" to "product"
            )
        }

        // Inglés → Español
        if (fromLang == "en" && toLang == "es") {
            return mapOf(
                "hello" to "hola",
                "thanks" to "gracias",
                "please" to "por favor",
                "yes" to "sí",
                "no" to "no",
                "good morning" to "buenos días",
                "good afternoon" to "buenas tardes",
                "good night" to "buenas noches",
                "price" to "precio",
                "buy" to "comprar",
                "product" to "producto"
            )
        }

        // Español → Portugués
        if (fromLang == "es" && toLang == "pt") {
            return mapOf(
                "hola" to "olá",
                "gracias" to "obrigado",
                "por favor" to "por favor",
                "sí" to "sim",
                "no" to "não",
                "buenos días" to "bom dia",
                "buenas tardes" to "boa tarde",
                "buenas noches" to "boa noite",
                "precio" to "preço",
                "comprar" to "comprar",
                "producto" to "produto"
            )
        }

        // Portugués → Español
        if (fromLang == "pt" && toLang == "es") {
            return mapOf(
                "olá" to "hola",
                "obrigado" to "gracias",
                "por favor" to "por favor",
                "sim" to "sí",
                "não" to "no",
                "bom dia" to "buenos días",
                "boa tarde" to "buenas tardes",
                "boa noite" to "buenas noches",
                "preço" to "precio",
                "comprar" to "comprar",
                "produto" to "producto"
            )
        }

        return emptyMap()
    }

    // ==================== CONFIGURACIÓN ====================

    /**
     * Obtener idioma del sistema
     */
    fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }

    /**
     * Obtener nombre del idioma
     */
    fun getLanguageName(code: String): String {
        return SUPPORTED_LANGUAGES.find { it.code == code }?.name ?: code
    }

    /**
     * Obtener flag del idioma
     */
    fun getLanguageFlag(code: String): String {
        return SUPPORTED_LANGUAGES.find { it.code == code }?.flag ?: "🌐"
    }

    /**
     * Verificar si el idioma es soportado
     */
    fun isLanguageSupported(code: String): Boolean {
        return SUPPORTED_LANGUAGES.any { it.code == code && it.supported }
    }

    /**
     * Obtener todos los idiomas soportados
     */
    fun getSupportedLanguages(): List<LanguageConfig> = SUPPORTED_LANGUAGES

    // ==================== ESTADÍSTICAS ====================

    /**
     * Registrar traducción
     */
    fun logTranslation(fromLang: String, toLang: String) {
        val key = "trans_${fromLang}_$toLang"
        val count = prefs.getInt(key, 0)
        prefs.edit().putInt(key, count + 1).apply()
    }

    /**
     * Obtener estadísticas de traducción
     */
    fun getTranslationStats(): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("trans_") && value is Int) {
                stats[key] = value
            }
        }
        return stats
    }
}
