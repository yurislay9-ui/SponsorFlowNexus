/*
 * Keyword Auto-Response Models - Data classes and enums
 */
package com.sponsorflow.nexus.autoresponder

/**
 * Tipo de acción cuando se detecta un keyword
 */
enum class KeywordActionType {
    SEND_MESSAGE,      // Enviar mensaje de texto
    SEND_PRODUCT,      // Enviar producto específico
    SEND_CATALOG,     // Enviar catálogo completo
    SEND_PRICE_LIST,  // Enviar lista de precios
    ESCALATE_HUMAN,   // Escalar a humano
    START_CHECKOUT,   // Iniciar proceso de compra
    SEND_LOCATION,     // Enviar ubicación
    SEND_WORKING_HOURS,// Enviar horarios
    SEND_SHIPPING_INFO,// Enviar info de envío
    SEND_PAYMENT_INFO, // Enviar info de pago
    APPLY_DISCOUNT,   // Aplicar descuento
    CUSTOM            // Acción custom
}

/**
 * Condición para activar el keyword
 */
enum class KeywordMatchType {
    EXACT,       // Coincidencia exacta
    CONTAINS,    // Contiene la palabra
    STARTS_WITH, // Empieza con
    ENDS_WITH,   // Termina con
    REGEX        // Expresión regular
}

/**
 * Respuesta asociada a un keyword
 */
data class KeywordResponse(
    val id: String,
    val keywords: List<String>,
    val matchType: KeywordMatchType = KeywordMatchType.CONTAINS,
    val actionType: KeywordActionType,
    val responseMessage: String? = null,
    val productId: String? = null,
    val catalogId: String? = null,
    val discountCode: String? = null,
    val discountPercent: Int? = null,
    val customData: Map<String, String>? = null,
    val priority: Int = 0,
    val isActive: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val responseCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Registro de uso de keyword
 */
data class KeywordUsage(
    val id: String,
    val keywordId: String,
    val keyword: String,
    val phone: String,
    val matchedAt: Long,
    val actionTaken: KeywordActionType,
    val wasHelpful: Boolean? = null
)

/**
 * Regla de autorespuesta completa
 */
data class AutoResponseRule(
    val id: String,
    val name: String,
    val description: String? = null,
    val responses: List<KeywordResponse>,
    val isActive: Boolean = true,
    val isGlobal: Boolean = false,
    val productIds: List<String>? = null,
    val categoryIds: List<String>? = null,
    val subscriptionTiers: List<String>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Configuración global de autorespuestas
 */
data class AutoResponseConfig(
    val enabled: Boolean = true,
    val caseSensitive: Boolean = false,
    val defaultResponse: String? = null,
    val fallbackToAI: Boolean = true,
    val requireExplicitMatch: Boolean = false,
    val maxResponsesPerMessage: Int = 3,
    val cooldownSeconds: Int = 60,
    val analyticsEnabled: Boolean = true
)

/**
 * Resultado de búsqueda de keyword
 */
data class KeywordMatchResult(
    val response: KeywordResponse?,
    val actionType: KeywordActionType?,
    val message: String?,
    val productId: String? = null,
    val catalogId: String? = null,
    val discountCode: String? = null,
    val discountPercent: Int? = null
)

/**
 * Estadísticas de keyword
 */
data class KeywordStats(
    val keyword: String,
    val responseCount: Int,
    val lastUsed: Long?
)
