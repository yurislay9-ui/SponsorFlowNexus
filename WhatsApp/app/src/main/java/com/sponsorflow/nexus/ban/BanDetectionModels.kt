/*
 * Ban Detection Models - Data classes and enums
 */
package com.sponsorflow.nexus.ban

/**
 * Tipo de evento de riesgo
 */
enum class RiskEventType {
    MESSAGE_FAILED,      // Mensaje falló
    RATE_LIMIT,         // Límite alcanzado
    USER_BLOCKED,       // Usuario te bloqueó
    USER_REPORTED,      // Usuario reportó como spam
    PHONE_DISABLED,     // Teléfono desactivado
    API_ERROR,          // Error de API
    SUSPICIOUS_ACTIVITY // Actividad sospechosa detectada
}

/**
 * Severidad del evento
 */
enum class EventSeverity {
    LOW,      // Información
    MEDIUM,   // Advertencia
    HIGH,     // Peligro
    CRITICAL  // Bloqueo inminente
}

/**
 * Evento de riesgo registrado
 */
data class RiskEvent(
    val id: String,
    val type: RiskEventType,
    val phoneNumber: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String? = null,
    val severity: EventSeverity = EventSeverity.LOW
)

/**
 * Nivel de riesgo de ban
 */
enum class BanRiskLevel(
    val displayName: String,
    val color: String,
    val action: String
) {
    SAFE("Seguro", "green", "Normal"),
    LOW("Bajo", "yellow", "Precaución"),
    MEDIUM("Medio", "orange", "Reducir actividad"),
    HIGH("Alto", "red", "Pausar inmediatamente"),
    CRITICAL("Crítico", "darkred", "Apagar app")
}

/**
 * Estado de riesgo de un número
 */
data class PhoneRiskStatus(
    val phoneNumber: String,
    val riskScore: Int = 0,
    val riskLevel: BanRiskLevel = BanRiskLevel.SAFE,
    val recentEvents: List<RiskEvent> = emptyList(),
    val blockCount7Days: Int = 0,
    val reportCount7Days: Int = 0,
    val failedCount7Days: Int = 0,
    val isPaused: Boolean = false,
    val pauseReason: String? = null,
    val lastWarningShown: Long? = null
)

/**
 * Alerta de bloqueo
 */
data class BanAlert(
    val id: String,
    val level: BanRiskLevel,
    val title: String,
    val message: String,
    val phoneNumber: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val actionRequired: String? = null,
    val autoActionTaken: Boolean = false
)

/**
 * Configuración de detección
 */
data class BanDetectionConfig(
    val enabled: Boolean = true,
    val blockThreshold7Days: Int = 3,
    val reportThreshold7Days: Int = 2,
    val failedThreshold7Days: Int = 10,
    val blockScore: Int = 30,
    val reportScore: Int = 40,
    val failedScore: Int = 5,
    val lowRiskThreshold: Int = 20,
    val mediumRiskThreshold: Int = 40,
    val highRiskThreshold: Int = 70,
    val criticalRiskThreshold: Int = 90,
    val autoPauseEnabled: Boolean = true,
    val autoPauseOnHighRisk: Boolean = true,
    val autoPauseOnCriticalRisk: Boolean = true,
    val notifyOnHighRisk: Boolean = true,
    val notifyOnCriticalRisk: Boolean = true
)

/**
 * Estadísticas de ban
 */
data class BanStats(
    val totalPhones: Int,
    val safeCount: Int,
    val lowCount: Int,
    val mediumCount: Int,
    val highCount: Int,
    val criticalCount: Int,
    val pendingAlerts: Int
)
