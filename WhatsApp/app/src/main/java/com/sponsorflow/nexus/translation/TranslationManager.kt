/*
 * Translation Manager (Compact)
 */
package com.sponsorflow.nexus.translation

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.ConcurrentHashMap

class TranslationManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_translation", Context.MODE_PRIVATE)
    private val languageCache = ConcurrentHashMap<String, String>()

    fun detectLanguage(text: String): String {
        val lower = text.lowercase()
        var spanishCount = TranslationConstants.SPANISH_INDICATORS.count { lower.contains(it) }
        var portugueseCount = TranslationConstants.PORTUGUESE_INDICATORS.count { lower.contains(it) }
        var englishCount = TranslationConstants.ENGLISH_INDICATORS.count { lower.contains(it) }
        return when {
            spanishCount >= portugueseCount && spanishCount >= englishCount -> "es"
            portugueseCount > englishCount -> "pt"
            englishCount > 0 -> "en"
            else -> "es"
        }
    }

    fun translate(text: String, targetLang: String, sourceLang: String? = null): TranslationResult {
        val source = sourceLang ?: detectLanguage(text)
        if (source == targetLang) return TranslationResult(text, text, source, targetLang, 1f, false)
        // Placeholder: real implementation would call translation API
        return TranslationResult(text, "[Translated: $text]", source, targetLang, 0.9f, true)
    }

    fun setLanguageForPhone(phone: String, lang: String) { languageCache[phone] = lang }
    fun getLanguageForPhone(phone: String): String? = languageCache[phone]
    fun getSupportedLanguages() = TranslationConstants.SUPPORTED_LANGUAGES
}
