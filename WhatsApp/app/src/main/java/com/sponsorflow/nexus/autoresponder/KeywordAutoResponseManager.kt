/*
 * Keyword Auto-Response Manager (Compact)
 */
package com.sponsorflow.nexus.autoresponder

import android.content.Context
import android.content.SharedPreferences

class KeywordAutoResponseManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_keywords", Context.MODE_PRIVATE)
    private val responses = mutableListOf<KeywordResponse>()

    fun addResponse(keywords: List<String>, action: KeywordActionType, message: String?): KeywordResponse {
        val r = KeywordResponse("kw_${System.currentTimeMillis()}", keywords, KeywordMatchType.CONTAINS, action, message)
        responses.add(r)
        return r
    }

    fun processMessage(message: String): KeywordMatchResult? {
        val lower = message.lowercase()
        return responses.find { r -> r.isActive && r.keywords.any { lower.contains(it.lowercase()) } }?.let {
            KeywordMatchResult(it, it.actionType, it.responseMessage)
        }
    }

    fun getResponses(): List<KeywordResponse> = responses.toList()
    fun setEnabled(enabled: Boolean) { prefs.edit().putBoolean("enabled", enabled).apply() }
    fun loadFromPrefs() { /* Load from prefs */ }
}
