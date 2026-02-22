/*
 * SponsorFlow Nexus v2.3 - Multi-Language Manager
 * Plan: PRO y ENTERPRISE
 */
package com.sponsorflow.nexus.language

import android.content.Context
import java.util.Locale

enum class SupportedLanguage(val code: String, val displayName: String) {
    ES("es", "Español"),
    EN("en", "English"),
    PT("pt", "Português"),
    FR("fr", "Français"),
    DE("de", "Deutsch")
}

class LanguageManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("nexus_lang", Context.MODE_PRIVATE)

    fun detectLanguage(message: String): SupportedLanguage {
        val lower = message.lowercase()
        
        // Detección simple por palabras comunes
        return when {
            matchesLanguage(lower, spanishWords) -> SupportedLanguage.ES
            matchesLanguage(lower, englishWords) -> SupportedLanguage.EN
            matchesLanguage(lower, portugueseWords) -> SupportedLanguage.PT
            matchesLanguage(lower, frenchWords) -> SupportedLanguage.FR
            matchesLanguage(lower, germanWords) -> SupportedLanguage.DE
            else -> SupportedLanguage.ES // Default
        }
    }

    private fun matchesLanguage(text: String, words: List<String>): Boolean {
        return words.any { text.contains(it) }
    }

    fun getResponseLanguage(phone: String): SupportedLanguage {
        val code = prefs.getString("client_$phone", "es") ?: "es"
        return SupportedLanguage.values().find { it.code == code } ?: SupportedLanguage.ES
    }

    fun setClientLanguage(phone: String, lang: SupportedLanguage) {
        prefs.edit().putString("client_$phone", lang.code).apply()
    }

    companion object {
        private val spanishWords = listOf("hola", "gracias", "buenos", "cómo", "precio", "producto")
        private val englishWords = listOf("hello", "thanks", "good", "how", "price", "product")
        private val portugueseWords = listOf("olá", "obrigado", "bom", "como", "preço", "produto")
        private val frenchWords = listOf("bonjour", "merci", "bien", "comment", "prix")
        private val germanWords = listOf("hallo", "danke", "gut", "wie", "preis")
    }
}