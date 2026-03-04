/*
 * Training Manager (Compact)
 */
package com.sponsorflow.nexus.training

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class TrainingManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("nexus_training", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val clients = mutableMapOf<String, ClientTraining>()

    fun createClient(phone: String, businessName: String, businessType: String, tone: String): ClientTraining {
        val client = ClientTraining(phone, businessName, businessType, tone)
        clients[phone] = client
        return client
    }

    fun getClient(phone: String): ClientTraining? = clients[phone]
    fun updateClient(client: ClientTraining) { clients[client.phone] = client }
    fun addFAQ(phone: String, q: String, a: String): Boolean {
        clients[phone]?.let { c ->
            clients[phone] = c.copy(faq = c.faq + QAPair("faq_${System.currentTimeMillis()}", q, a))
            return true
        }
        return false
    }

    fun generatePrompt(phone: String): String? {
        val c = clients[phone] ?: return null
        return "Eres ${c.businessName}. Tono: ${c.tone}. Productos: ${c.products.joinToString { it.name }}"
    }

    fun loadFromPrefs() { prefs.getString("clients", null)?.let { clients.putAll(gson.fromJson(it)) } }
    fun saveToPrefs() { prefs.edit().putString("clients", gson.toJson(clients)).apply() }
}

data class ClientTraining(val phone: String, val businessName: String, val businessType: String, val tone: String, val language: String = "es", val trainingItems: List<TrainingItem> = emptyList(), val customRules: List<String> = emptyList(), val keywords: List<String> = emptyList(), val faq: List<QAPair> = emptyList(), val products: List<ProductInfo> = emptyList(), val createdAt: Long = System.currentTimeMillis(), val updatedAt: Long = System.currentTimeMillis())
