/*
 * SponsorFlow Nexus v1.0 - Accessibility Service
 * CORREGIDO: MessageHandler implementado, mutableSetOf -> ConcurrentHashMap
 */
package com.sponsorflow.nexus.core

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.sponsorflow.nexus.core.enums.OperationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class NexusAccessibilityService : AccessibilityService() {

    private var operationStatus: OperationStatus = OperationStatus.IDLE
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var messageHandler: MessageHandler

    override fun onServiceConnected() {
        super.onServiceConnected()
        messageHandler = MessageHandler(this)
        operationStatus = OperationStatus.RUNNING
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!operationStatus.canProcess()) return
        event ?: return

        if (event.packageName == "com.whatsapp" || event.packageName == "com.whatsapp.w4b") {
            when (event.eventType) {
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    handleNotification(event)
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    handleWindowChange(event)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    handleWindowContentChange(event)
                }
            }
        }
    }

    override fun onInterrupt() {
        operationStatus = OperationStatus.ERROR
    }

    override fun onDestroy() {
        operationStatus = OperationStatus.STOPPED
        super.onDestroy()
    }

    private fun handleNotification(event: AccessibilityEvent) {
        val text = event.text?.joinToString(" ") ?: return
        if (text.isNotEmpty()) {
            serviceScope.launch {
                messageHandler.processIncoming(text)
            }
        }
    }

    private fun handleWindowChange(event: AccessibilityEvent) {
        val className = event.className?.toString() ?: return
        if (className.contains("ConversationActivity")) {
            messageHandler.onChatOpened()
        }
    }

    private fun handleWindowContentChange(event: AccessibilityEvent) {
        // Manejar cambios en el contenido de la ventana
    }

    fun setOperationStatus(status: OperationStatus) {
        operationStatus = status
    }

    fun getOperationStatus(): OperationStatus = operationStatus
}

/**
 * MessageHandler - Procesa mensajes entrantes de WhatsApp
 * CORREGIDO: Thread-safe con ConcurrentHashMap
 */
class MessageHandler(
    private val service: AccessibilityService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // CORREGIDO: ConcurrentHashMap.newKeySet() para thread-safety
    private val processedMessages = ConcurrentHashMap.newKeySet<String>()
    private val maxCacheSize = 100

    /**
     * Procesar mensaje entrante desde notificación
     */
    suspend fun processIncoming(text: String) {
        // Evitar duplicados - thread-safe
        if (processedMessages.contains(text) || text.isBlank()) return
        
        // Agregar al cache
        addToCache(text)
        
        android.util.Log.d("MessageHandler", "Mensaje recibido: $text")
    }
    
    /**
     * Called when a chat window is opened
     */
    fun onChatOpened() {
        android.util.Log.d("MessageHandler", "Chat de WhatsApp abierto")
    }

    /**
     * Enviar respuesta mediante accesibilidad
     */
    fun sendReply(message: String): Boolean {
        return try {
            val inputField = findInputField() ?: return false
            
            inputField.text?.clear()
            inputField.text?.append(message)
            
            val sendButton = findSendButton()
            if (sendButton != null) {
                sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("MessageHandler", "Error enviando respuesta: ${e.message}")
            false
        }
    }

    /**
     * Encontrar el campo de entrada de texto
     */
    private fun findInputField(): AccessibilityNodeInfo? {
        val rootNode = service.rootInActiveWindow ?: return null
        
        val inputFields = rootNode.findAccessibilityNodeInfosByViewId(
            "com.whatsapp:id/entry"
        )
        
        if (inputFields.isEmpty()) {
            val textFields = rootNode.findAccessibilityNodeInfosByText("Mensaje")
            return textFields.firstOrNull()
        }
        
        return inputFields.firstOrNull()
    }

    /**
     * Encontrar el botón de enviar
     */
    private fun findSendButton(): AccessibilityNodeInfo? {
        val rootNode = service.rootInActiveWindow ?: return null
        
        val sendButtons = rootNode.findAccessibilityNodeInfosByViewId(
            "com.whatsapp:id/send"
        )
        
        return sendButtons.firstOrNull()
    }

    /**
     * Agregar mensaje al cache con límite de tamaño
     */
    private fun addToCache(message: String) {
        if (processedMessages.size >= maxCacheSize) {
            processedMessages.clear()
        }
        processedMessages.add(message)
    }

    /**
     * Limpiar cache de mensajes procesados
     */
    fun clearCache() {
        processedMessages.clear()
    }
}
