/*
 * SponsorFlow Nexus v2.4 - Plugin Types
 * Plan: ENTERPRISE
 * CORREGIDO: Version actualizada a v2.4
 */
package com.sponsorflow.nexus.plugin

enum class PluginType {
    RESPONSE,    // Modifica respuestas
    ACTION,      // Ejecuta acciones
    ANALYZER,    // Analiza mensajes
    INTEGRATION  // Conecta servicios externos
}

data class PluginInfo(
    val id: String,
    val name: String,
    val version: String,
    val type: PluginType,
    val author: String,
    val description: String,
    val enabled: Boolean = true
)

data class PluginResult(
    val success: Boolean,
    val data: Map<String, Any> = emptyMap(),
    val error: String? = null
)
