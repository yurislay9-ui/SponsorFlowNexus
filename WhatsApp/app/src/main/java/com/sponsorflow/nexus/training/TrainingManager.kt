/*
 * Training Manager (Compact)
 */
package com.sponsorflow.nexus.training

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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

    fun loadFromPrefs() {
        prefs.getString("clients", null)?.let { json ->
            val type = object : TypeToken<Map<String, ClientTraining>>() {}.type
            val loadedClients: Map<String, ClientTraining> = gson.fromJson(json, type)
            clients.putAll(loadedClients)
        }
    }
    fun saveToPrefs() { prefs.edit().putString("clients", gson.toJson(clients)).apply() }
}
