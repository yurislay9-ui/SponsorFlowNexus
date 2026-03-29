/*
 * SponsorFlow Nexus v1.0 - Structured Logger
 * CORREGIDO: MEDIUM-003 - Sistema de logging estructurado
 */
package com.sponsorflow.nexus.core

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sistema de logging estructurado para la aplicación.
 * Proporciona formato consistente y filtrado por categorías.
 */
object NexusLogger {
    
    private const val TAG_PREFIX = "Nexus"
    private const val MAX_TAG_LENGTH = 23
    private var isDebugMode = true
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    
    enum class Level {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * Configura el modo debug (habilita/deshabilita logs verbose/debug)
     */
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
    }
    
    private fun formatTag(category: String): String {
        val tag = "$TAG_PREFIX/$category"
        return if (tag.length > MAX_TAG_LENGTH) tag.substring(0, MAX_TAG_LENGTH) else tag
    }
    
    private fun formatMessage(message: String, data: Map<String, Any>? = null): String {
        val timestamp = dateFormat.format(Date())
        val builder = StringBuilder("[$timestamp] $message")
        data?.forEach { (key, value) ->
            builder.append(" | $key=$value")
        }
        return builder.toString()
    }
    
    // ==================== General Logging ====================
    
    fun v(category: String, message: String, data: Map<String, Any>? = null) {
        if (isDebugMode) {
            Log.v(formatTag(category), formatMessage(message, data))
        }
    }
    
    fun d(category: String, message: String, data: Map<String, Any>? = null) {
        if (isDebugMode) {
            Log.d(formatTag(category), formatMessage(message, data))
        }
    }
    
    fun i(category: String, message: String, data: Map<String, Any>? = null) {
        Log.i(formatTag(category), formatMessage(message, data))
    }
    
    fun w(category: String, message: String, data: Map<String, Any>? = null) {
        Log.w(formatTag(category), formatMessage(message, data))
    }
    
    fun e(category: String, message: String, throwable: Throwable? = null, data: Map<String, Any>? = null) {
        val formattedMessage = formatMessage(message, data)
        if (throwable != null) {
            Log.e(formatTag(category), formattedMessage, throwable)
        } else {
            Log.e(formatTag(category), formattedMessage)
        }
    }
    
    // ==================== Category-Specific Helpers ====================
    
    object Network {
        private const val CATEGORY = "Network"
        
        fun request(endpoint: String, method: String, durationMs: Long? = null) {
            val data: MutableMap<String, Any> = mutableMapOf("endpoint" to endpoint, "method" to method)
            durationMs?.let { data["duration_ms"] = it }
            d(CATEGORY, "API Request", data)
        }
        
        fun response(endpoint: String, code: Int, durationMs: Long) {
            d(CATEGORY, "API Response", mapOf(
                "endpoint" to endpoint,
                "code" to code,
                "duration_ms" to durationMs
            ))
        }
        
        fun error(endpoint: String, error: Throwable) {
            e(CATEGORY, "Network Error", error, mapOf("endpoint" to endpoint))
        }
    }
    
    object AI {
        private const val CATEGORY = "AI"
        
        fun modelLoaded(modelName: String, sizeMB: Long) {
            i(CATEGORY, "Model loaded", mapOf("model" to modelName, "size_mb" to sizeMB))
        }
        
        fun inference(prompt: String, tokens: Int, durationMs: Long) {
            d(CATEGORY, "Inference", mapOf(
                "prompt_length" to prompt.length,
                "tokens" to tokens,
                "duration_ms" to durationMs
            ))
        }
        
        fun error(operation: String, error: Throwable) {
            e(CATEGORY, "AI Error: $operation", error)
        }
    }
    
    object Payment {
        private const val CATEGORY = "Payment"
        
        fun transaction(txHash: String, amount: Long, status: String) {
            i(CATEGORY, "Transaction", mapOf(
                "tx_hash" to txHash.take(16),
                "amount" to amount,
                "status" to status
            ))
        }
        
        fun validation(address: String, valid: Boolean) {
            d(CATEGORY, "Address validation", mapOf(
                "address" to address.take(16),
                "valid" to valid
            ))
        }
    }
    
    object License {
        private const val CATEGORY = "License"
        
        fun validationSuccess(tier: String, expiresAt: Long) {
            i(CATEGORY, "License valid", mapOf("tier" to tier, "expires_at" to expiresAt))
        }
        
        fun validationFailed(reason: String) {
            w(CATEGORY, "License invalid", mapOf("reason" to reason))
        }
    }
    
    object Cache {
        private const val CATEGORY = "Cache"
        
        fun hit(key: String) {
            v(CATEGORY, "Cache hit", mapOf("key" to key.take(32)))
        }
        
        fun miss(key: String) {
            v(CATEGORY, "Cache miss", mapOf("key" to key.take(32)))
        }
        
        fun cleared(count: Int) {
            i(CATEGORY, "Cache cleared", mapOf("entries" to count))
        }
    }
    
    object Security {
        private const val CATEGORY = "Security"
        
        fun integrityCheck(passed: Boolean, checks: Int) {
            i(CATEGORY, "Integrity check", mapOf("passed" to passed, "checks" to checks))
        }
        
        fun suspiciousActivity(type: String, details: String) {
            w(CATEGORY, "Suspicious activity: $type", mapOf("details" to details))
        }
    }
}