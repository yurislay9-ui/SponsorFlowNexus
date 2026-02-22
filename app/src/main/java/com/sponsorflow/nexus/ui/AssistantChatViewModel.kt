/*
 * SponsorFlow Nexus - Assistant Chat ViewModel
 */
package com.sponsorflow.nexus.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AssistantChatViewModel : ViewModel() {
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages.toList()
    
    var inputText by mutableStateOf("")
        private set
    
    init {
        _messages.add(ChatMessage(
            text = "¡Hola! Soy el asistente de SponsorFlow Nexus. ¿En qué puedo ayudarte?",
            isFromUser = false
        ))
    }
    
    fun onInputChange(text: String) { inputText = text }
    
    fun sendMessage() {
        if (inputText.isBlank()) return
        _messages.add(ChatMessage(text = inputText, isFromUser = true))
        val userQuestion = inputText
        inputText = ""
        val response = generateResponse(userQuestion)
        _messages.add(ChatMessage(text = response, isFromUser = false))
    }
    
    private fun generateResponse(question: String): String {
        val q = question.lowercase()
        return when {
            q.contains("plan") || q.contains("precio") || q.contains("suscripci") ->
                "Tenemos 4 planes: Gratis ($0), Observador ($9/mes), Desarrollo ($19/mes), Empresario ($29/mes)"
            q.contains("pagar") || q.contains("pago") || q.contains("usdt") ->
                "Para pagar necesitas una wallet TRON. Escanea el código QR y envía el monto en USDT."
            q.contains("ia") || q.contains("memoria") ->
                "Memoria IA por plan: Gratis (0), Observador (5), Desarrollo (20), Empresario (ilimitada)"
            q.contains("hola") || q.contains("buenas") -> "¡Hola! ¿En qué puedo ayudarte?"
            q.contains("ayuda") || q.contains("problema") -> "Cuéntame tu problema y te ayudaré."
            else -> "Para más info: soporte@sponsorflow.com"
        }
    }
}