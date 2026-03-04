/*
 * Translation Models
 */
package com.sponsorflow.nexus.translation

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

object TranslationConstants {
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
        LanguageConfig("ru", "Ruso", "Русский", "🇷🇺")
    )
    
    val SPANISH_INDICATORS = listOf("hola", "buenos", "gracias", "por favor", "quiero", "tengo", "cuánto", "cuál", "dónde", "cuándo", "cómo", "qué", "sí", "no", "buenos días", "buenas tardes", "está", "tienen")
    val PORTUGUESE_INDICATORS = listOf("olá", "bom", "obrigado", "por favor", "quero", "tenho", "quanto", "qual", "onde", "como", "sim", "não", "bom dia", "boa tarde")
    val ENGLISH_INDICATORS = listOf("hello", "hi", "thanks", "thank you", "please", "want", "have", "how", "what", "where", "when", "why", "yes", "no", "good morning", "good afternoon", "is", "are")
}
