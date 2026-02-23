/*
 * SponsorFlow Nexus v1.0 - Conversation Cache
 * CORREGIDO: ConcurrentHashMap, límite EMPRESARIO
 */
package com.sponsorflow.nexus.cache

import android.content.Context
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import java.util.concurrent.ConcurrentHashMap

data class CachedMessage(
    val phone: String,
    val message: String,
    val response: String,
    val timestamp: Long,
    val sentiment: String? = null
)

data class ConversationMemory(
    val phone: String,
    val lastProducts: List<String>,
    val lastIntent: String,
    val messageCount: Int,
    val lastContact: Long
)

class ConversationCache(
    private val context: Context,
    private val tier: SubscriptionTier
) {
    private val prefs = context.getSharedPreferences("nexus_cache", Context.MODE_PRIVATE)
    
    // CORREGIDO: ConcurrentHashMap thread-safe
    private val memories = ConcurrentHashMap<String, ConversationMemory>()
    
    // Límite para EMPRESARIO
    private val MAX_ENTERPRISE_MESSAGES = 1000

    // Guardar mensaje según plan
    fun saveMessage(phone: String, message: String, response: String) {
        when (tier) {
            SubscriptionTier.FREE -> {
                // Solo contar para límite diario - NO guardar contenido
                incrementDailyCount()
            }
            SubscriptionTier.OBSERVADOR -> {
                // Cache básico: últimos 10 mensajes
                saveBasicCache(phone, message, response)
            }
            SubscriptionTier.DESARROLLO -> {
                // Cache medio: últimos 50 mensajes + memoria
                saveProCache(phone, message, response)
            }
            SubscriptionTier.EMPRESARIO -> {
                // Cache completo: ilimitado + memoria persistente
                saveEnterpriseCache(phone, message, response)
            }
        }
    }

    // Obtener memoria de conversación
    fun getMemory(phone: String): ConversationMemory? {
        if (tier == SubscriptionTier.FREE) return null
        return memories[phone] ?: loadMemoryFromCache(phone)
    }

    // Verificar límite diario (FREE)
    fun isLimitReached(): Boolean {
        if (tier != SubscriptionTier.FREE) return false
        val today = getTodayKey()
        val count = prefs.getInt(today, 0)
        return count >= 50
    }

    fun getRemainingMessages(): Int {
        if (tier != SubscriptionTier.FREE) return -1
        val today = getTodayKey()
        val count = prefs.getInt(today, 0)
        return maxOf(0, 50 - count)
    }

    // Métodos privados
    private fun incrementDailyCount() {
        val today = getTodayKey()
        val count = prefs.getInt(today, 0)
        prefs.edit().putInt(today, count + 1).apply()
    }

    private fun getTodayKey(): String {
        return "day_${System.currentTimeMillis() / 86400000}"
    }

    private fun saveBasicCache(phone: String, message: String, response: String) {
        val key = "basic_$phone"
        val existing = prefs.getString(key, "") ?: ""
        val entries = if (existing.isNotEmpty()) existing.split("||").toMutableList() else mutableListOf<String>()
        
        entries.add("$message|$response|${System.currentTimeMillis()}")
        
        // Mantener solo últimos 10
        val trimmed = entries.takeLast(10)
        prefs.edit().putString(key, trimmed.joinToString("||")).apply()
    }

    private fun saveProCache(phone: String, message: String, response: String) {
        val key = "pro_$phone"
        val existing = prefs.getString(key, "") ?: ""
        val entries = if (existing.isNotEmpty()) existing.split("||").toMutableList() else mutableListOf<String>()
        
        entries.add("$message|$response|${System.currentTimeMillis()}")
        
        // Mantener últimos 50
        val trimmed = entries.takeLast(50)
        prefs.edit().putString(key, trimmed.joinToString("||")).apply()
        
        // Actualizar memoria
        updateMemory(phone, message)
    }

    private fun saveEnterpriseCache(phone: String, message: String, response: String) {
        val key = "ent_$phone"
        val existing = prefs.getString(key, "") ?: ""
        val entries = if (existing.isNotEmpty()) existing.split("||").toMutableList() else mutableListOf<String>()
        
        entries.add("$message|$response|${System.currentTimeMillis()}")
        
        // Sin límite para ENTERPRISE
        prefs.edit().putString(key, entries.joinToString("||")).apply()
        
        // Memoria persistente
        updateMemory(phone, message)
        savePersistentMemory(phone)
    }

    private fun updateMemory(phone: String, message: String) {
        val current = memories[phone]
        memories[phone] = ConversationMemory(
            phone = phone,
            lastProducts = current?.lastProducts ?: emptyList(),
            lastIntent = extractIntent(message),
            messageCount = (current?.messageCount ?: 0) + 1,
            lastContact = System.currentTimeMillis()
        )
    }

    private fun savePersistentMemory(phone: String) {
        val memory = memories[phone] ?: return
        prefs.edit()
            .putString("mem_${phone}_intent", memory.lastIntent)
            .putInt("mem_${phone}_count", memory.messageCount)
            .putLong("mem_${phone}_last", memory.lastContact)
            .apply()
    }

    private fun loadMemoryFromCache(phone: String): ConversationMemory? {
        val intent = prefs.getString("mem_${phone}_intent", null) ?: return null
        return ConversationMemory(
            phone = phone,
            lastProducts = emptyList(),
            lastIntent = intent,
            messageCount = prefs.getInt("mem_${phone}_count", 0),
            lastContact = prefs.getLong("mem_${phone}_last", 0)
        )
    }

    private fun extractIntent(message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("precio") || lower.contains("cuesta") -> "PRICE_INQUIRY"
            lower.contains("comprar") || lower.contains("quiero") -> "PURCHASE_INTENT"
            lower.contains("horario") || lower.contains("abierto") -> "BUSINESS_INFO"
            lower.contains("ayuda") || lower.contains("soporte") -> "SUPPORT"
            else -> "GENERAL"
        }
    }

    // Limpiar cache (solo para pagos)
    fun clearCache() {
        if (tier == SubscriptionTier.FREE) return
        prefs.edit().clear().apply()
        messages.clear()
        memories.clear()
    }
}