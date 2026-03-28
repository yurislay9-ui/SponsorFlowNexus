/*
 * Risk Level Models
 */
package com.sponsorflow.nexus.risk

enum class RiskLevel(
    val displayName: String,
    val description: String,
    val maxDaily: Int,
    val maxPerHour: Int,
    val delayMinMs: Long,
    val delayMaxMs: Long,
    val allowExpired: Boolean,
    val warningMessage: String
) {
    LOW("Bajo", "Máxima seguridad", 50, 10, 45000L, 120000L, false, "Muy seguro"),
    MEDIUM("Medio", "Balance seguridad/volumen", 88, 18, 30000L, 90000L, false, "Recomendado"),
    HIGH("Alto", "Más volumen", 120, 25, 15000L, 45000L, false, "Riesgo medio"),
    CRITICAL("Crítico", "Máximo volumen", 150, 35, 8000L, 25000L, true, "Riesgo alto")
}

data class RiskConfig(
    val level: RiskLevel = RiskLevel.MEDIUM,
    val enabled: Boolean = true,
    val customMaxDaily: Int? = null,
    val customMaxPerHour: Int? = null
)

data class RiskCheckResult(
    val canSend: Boolean,
    val delayMs: Long,
    val reason: String? = null,
    val riskLevel: RiskLevel
)
