/*
 * SponsorFlow Nexus v2.4 - WhatsApp Service Principal
 * CORREGIDO: ConcurrentLinkedQueue thread-safe, WeakReference para instance
 */
package com.sponsorflow.nexus.whatsapp

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.sponsorflow.nexus.subscription.SubscriptionGate
import com.sponsorflow.nexus.core.enums.SubscriptionTier
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

class WhatsAppService : AccessibilityService() {
    
    companion object { 
        const val WA_PACKAGE = "com.whatsapp"
        // CORREGIDO: WeakReference para evitar memory leaks
        private var instanceRef: WeakReference<WhatsAppService>? = null
        fun getInstance() = instanceRef?.get()
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var subGate: SubscriptionGate
    private var tier: SubscriptionTier = SubscriptionTier.FREE
    private var lastEvent = 0L
    
    // CORREGIDO: ConcurrentLinkedQueue thread-safe
    private val pending = ConcurrentLinkedQueue<Pair<String, String>>()
    
    override fun onServiceConnected() { 
        instanceRef = WeakReference(this)
    }
    
    fun setSubscriptionGate(gate: SubscriptionGate) { subGate = gate }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.packageName != WA_PACKAGE) return
        if (!::subGate.isInitialized || !subGate.canUseWhatsApp()) return
        if (System.currentTimeMillis() - lastEvent < 1500) return
        lastEvent = System.currentTimeMillis()
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> handleNotification(event)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> handleWindow()
        }
    }
    
    private fun handleNotification(event: AccessibilityEvent) {
        val text = event.text?.joinToString(" ") ?: return
        val parts = text.split(":", limit = 2)
        if (parts.size == 2) pending.offer(parts[0].trim() to parts[1].trim())
    }
    
    private fun handleWindow() {
        if (pending.isEmpty()) return
        val root = rootInActiveWindow ?: return
        val input = findNode(root, "entry") ?: return
        val sendBtn = findNode(root, "send") ?: return
        
        // CORREGIDO: Usar poll() thread-safe
        val item = pending.poll() ?: return
        val (_, msg) = item
        
        scope.launch(Dispatchers.Main) {
            // Anti-detección obligatorio via SubscriptionGate
            if (!subGate.applyAntiDetection(msg)) return@launch
            
            val response = generateResponse(msg)
            if (response.isNotEmpty()) {
                val finalText = subGate.processResponse(response)
                typeMessage(input, finalText)
                sendBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }
    
    private fun generateResponse(msg: String): String {
        // Usar NLP via SubscriptionGate (todas las suscripciones)
        val intent = subGate.detectIntent(msg)
        return when (intent) {
            "pago" -> "Gracias por preguntar. Te envío información de precios y métodos de pago."
            "ayuda" -> "Claro, estoy aquí para ayudarte. ¿Qué necesitas?"
            "producto" -> "Te muestro nuestro catálogo de productos disponibles."
            "pedido" -> "Perfecto, puedo ayudarte con tu pedido. ¿Qué deseas comprar?"
            "saludo" -> "¡Hola! ¿En qué puedo ayudarte hoy?"
            else -> ""
        }
    }
    
    private fun typeMessage(input: AccessibilityNodeInfo, text: String) {
        val args = Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }
    
    private fun findNode(root: AccessibilityNodeInfo, idSuffix: String): AccessibilityNodeInfo? {
        return root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/$idSuffix").firstOrNull()
    }
    
    fun setTier(t: SubscriptionTier) { tier = t }
    override fun onInterrupt() {}
    override fun onDestroy() { 
        super.onDestroy()
        scope.cancel()
        instanceRef?.clear()
        instanceRef = null
    }
}
