/*
 * SponsorFlow Nexus v2.3 - AI Memory Manager
 * Plan: OBSERVADOR, DESARROLLO, EMPRESARIO
 */
package com.sponsorflow.nexus.cache

import android.content.Context
import com.sponsorflow.nexus.core.enums.SubscriptionTier

data class AIContext(
    val phone: String,
    val conversationHistory: List<Pair<String, String>>,
    val userProfile: UserProfile,
    val lastProductsDiscussed: List<String>,
    val preferences: Map<String, String>
)

data class UserProfile(
    val name: String?,
    val language: String,
    val interests: List<String>,
    val purchaseHistory: List<String>
)

class AIMemoryManager(
    private val context: Context,
    private val tier: SubscriptionTier
) {
    private val prefs = context.getSharedPreferences("nexus_ai_memory", Context.MODE_PRIVATE)
    private val contextMap = mutableMapOf<String, AIContext>()

    // Construir contexto para la IA
    fun buildContext(phone: String, newMessage: String): String {
        if (tier == SubscriptionTier.OBSERVADOR) {
            // Sin memoria - cada mensaje es independiente
            return newMessage
        }

        val ctx = contextMap[phone] ?: loadContext(phone)
        
        return when (tier) {
            SubscriptionTier.OBSERVADOR -> buildBasicContext(ctx, newMessage)
            SubscriptionTier.DESARROLLO -> buildProContext(ctx, newMessage)
            SubscriptionTier.EMPRESARIO -> buildEnterpriseContext(ctx, newMessage)
            else -> newMessage
        }
    }

    // Actualizar memoria después de respuesta
    fun updateMemory(phone: String, message: String, response: String) {
        if (tier == SubscriptionTier.OBSERVADOR) return

        val current = contextMap[phone]
        
        contextMap[phone] = AIContext(
            phone = phone,
            conversationHistory = updateHistory(current?.conversationHistory, message, response),
            userProfile = current?.userProfile ?: UserProfile(null, "es", emptyList(), emptyList()),
            lastProductsDiscussed = extractProducts(message, response),
            preferences = current?.preferences ?: emptyMap()
        )

        saveContext(phone)
    }

    // Obtener historial para mostrar al usuario
    fun getHistory(phone: String): List<Pair<String, String>> {
        if (tier == SubscriptionTier.OBSERVADOR) return emptyList()
        return contextMap[phone]?.conversationHistory ?: loadHistory(phone)
    }

    // Métodos privados
    private fun buildBasicContext(ctx: AIContext?, message: String): String {
        // Solo últimos 3 mensajes como contexto
        val history = ctx?.conversationHistory?.takeLast(3) ?: emptyList()
        if (history.isEmpty()) return message
        
        val contextStr = history.joinToString("\n") { (q, a) -> 
            "Usuario: $q\nAsistente: $a" 
        }
        return "Contexto reciente:\n$contextStr\n\nMensaje actual: $message"
    }

    private fun buildProContext(ctx: AIContext?, message: String): String {
        val history = ctx?.conversationHistory?.takeLast(10) ?: emptyList()
        val products = ctx?.lastProductsDiscussed ?: emptyList()
        
        val contextStr = history.joinToString("\n") { (q, a) -> 
            "Usuario: $q\nAsistente: $a" 
        }
        val productsStr = if (products.isNotEmpty()) "\nProductos discutidos: ${products.joinToString()}" else ""
        
        return "Contexto:\n$contextStr$productsStr\n\nMensaje: $message"
    }

    private fun buildEnterpriseContext(ctx: AIContext?, message: String): String {
        val history = ctx?.conversationHistory ?: emptyList()
        val profile = ctx?.userProfile
        val products = ctx?.lastProductsDiscussed ?: emptyList()
        val preferences = ctx?.preferences ?: emptyMap()

        val profileStr = if (profile != null) {
            "\nPerfil: idioma=${profile.language}, intereses=${profile.interests.joinToString()}"
        } else ""

        val contextStr = history.takeLast(20).joinToString("\n") { (q, a) -> 
            "Usuario: $q\nAsistente: $a" 
        }

        return "Contexto completo:\n$contextStr$profileStr\nProductos: ${products.joinToString()}\n\nMensaje: $message"
    }

    private fun updateHistory(
        history: List<Pair<String, String>>?,
        message: String,
        response: String
    ): List<Pair<String, String>> {
        val newEntry = message to response
        val current = history?.toMutableList() ?: mutableListOf()
        current.add(newEntry)
        
        // Limitar según plan
        return when (tier) {
            SubscriptionTier.OBSERVADOR -> current.takeLast(10)
            SubscriptionTier.DESARROLLO -> current.takeLast(50)
            SubscriptionTier.EMPRESARIO -> current
            else -> current.takeLast(10)
        }
    }

    private fun extractProducts(message: String, response: String): List<String> {
        val products = mutableListOf<String>()
        val productKeywords = listOf("producto", "artículo", "servicio", "precio", "comprar")
        
        val text = "$message $response".lowercase()
        for (keyword in productKeywords) {
            if (text.contains(keyword)) {
                products.add(keyword)
            }
        }
        
        return products.distinct()
    }

    private fun saveContext(phone: String) {
        val ctx = contextMap[phone] ?: return
        val historyStr = ctx.conversationHistory.takeLast(20)
            .joinToString("||") { "${it.first}::${it.second}" }
        
        prefs.edit()
            .putString("ctx_${phone}_history", historyStr)
            .putString("ctx_${phone}_products", ctx.lastProductsDiscussed.joinToString(","))
            .apply()
    }

    private fun loadContext(phone: String): AIContext {
        val historyStr = prefs.getString("ctx_${phone}_history", "") ?: ""
        val productsStr = prefs.getString("ctx_${phone}_products", "") ?: ""

        val history = if (historyStr.isNotEmpty()) {
            historyStr.split("||").mapNotNull {
                val parts = it.split("::")
                if (parts.size == 2) parts[0] to parts[1] else null
            }
        } else emptyList()

        val products = if (productsStr.isNotEmpty()) {
            productsStr.split(",")
        } else emptyList()

        return AIContext(
            phone = phone,
            conversationHistory = history,
            userProfile = UserProfile(null, "es", emptyList(), emptyList()),
            lastProductsDiscussed = products,
            preferences = emptyMap()
        )
    }

    private fun loadHistory(phone: String): List<Pair<String, String>> {
        return loadContext(phone).conversationHistory
    }

    // Limpiar memoria
    fun clearMemory(phone: String) {
        contextMap.remove(phone)
        prefs.edit()
            .remove("ctx_${phone}_history")
            .remove("ctx_${phone}_products")
            .apply()
    }
}
