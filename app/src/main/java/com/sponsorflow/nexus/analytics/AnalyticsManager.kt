package com.sponsorflow.nexus.analytics

import android.content.Context
import com.sponsorflow.nexus.NexusLogger
import com.sponsorflow.nexus.config.FeatureFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AnalyticsManager(private val context: Context) {
    
    private val featureFlags = FeatureFlags(context)
    private val eventQueue = ConcurrentHashMap<String, Any>()
    private val scope = CoroutineScope(Dispatchers.IO)
    
    suspend fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        withContext(Dispatchers.IO) {
            if (!isAnalyticsEnabled()) return@withContext
            
            val event = mapOf(
                "event_name" to eventName,
                "timestamp" to System.currentTimeMillis(),
                "params" to params,
                "session_id" to getSessionId()
            )
            
            eventQueue[eventName] = event
            sendEventToServer(event)
        }
    }
    
    suspend fun trackScreenView(screenName: String) {
        trackEvent("screen_view", mapOf("screen_name" to screenName))
    }
    
    suspend fun trackError(errorName: String, errorMessage: String) {
        trackEvent("error", mapOf(
            "error_name" to errorName,
            "error_message" to errorMessage
        ))
    }
    
    suspend fun trackUserAction(actionName: String, actionData: Map<String, Any> = emptyMap()) {
        trackEvent("user_action", mapOf(
            "action_name" to actionName,
            "action_data" to actionData
        ))
    }
    
    private suspend fun isAnalyticsEnabled(): Boolean {
        return featureFlags.isAdvancedAnalyticsEnabled.firstOrNull() ?: false
    }
    
    private suspend fun sendEventToServer(event: Map<String, Any>) {
        try {
            // Simulación de envío a servidor
            NexusLogger.info("Analytics event sent: ${event["event_name"]}")
        } catch (e: Exception) {
            NexusLogger.error("Failed to send analytics event", e)
        }
    }
    
    private fun getSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}