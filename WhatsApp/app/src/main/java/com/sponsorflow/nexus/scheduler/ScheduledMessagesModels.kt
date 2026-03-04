/*
 * Scheduled Messages Models - Data classes and enums
 */
package com.sponsorflow.nexus.scheduler

/**
 * Tipo de mensaje programado
 */
enum class ScheduledMessageType(
    val displayName: String,
    val description: String
) {
    FOLLOWUP_LEAD(
        "Seguimiento de Lead",
        "Cliente interesado pero sin comprar"
    ),
    PAYMENT_REMINDER(
        "Recordatorio de Pago",
        "Cliente con pago pendiente"
    ),
    POST_PURCHASE(
        "Seguimiento Post-Venta",
        "Después de una compra"
    ),
    PROMOTION(
        "Promoción",
        "Oferta especial o descuento"
    ),
    CUSTOM(
        "Personalizado",
        "Mensaje customizado"
    )
}

/**
 * Estado del mensaje
 */
enum class ScheduledMessageStatus {
    PENDING,    // Esperando para enviar
    SENT,       // Enviado
    FAILED,     // Fallido
    CANCELLED,  // Cancelado
    EXPIRED     // Expirado
}

/**
 * Frecuencia de repetición
 */
enum class RepeatFrequency {
    ONCE,          // Una vez
    DAILY,         // Diario
    WEEKLY,        // Semanal
    MONTHLY,       // Mensual
    CUSTOM_DAYS    // Custom (X días)
}

/**
 * Tipo de disparador
 */
enum class TriggerType {
    ABSOLUTE,        // Fecha específica
    AFTER_PURCHASE,  // X días después de compra
    AFTER_INACTIVITY, // X días de inactividad
    AFTER_INTEREST,  // X días después de mostrar interés
    CONDITION        // Cuando se cumpla una condición
}

/**
 * Mensaje programado
 */
data class ScheduledMessage(
    val id: String,
    val type: ScheduledMessageType,
    val title: String,
    val message: String,
    val phone: String,
    val customerName: String? = null,
    val triggerType: TriggerType,
    val triggerValue: Long,
    val repeat: RepeatFrequency = RepeatFrequency.ONCE,
    val repeatDays: Int? = null,
    val status: ScheduledMessageStatus = ScheduledMessageStatus.PENDING,
    val sentAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSentAt: Long? = null,
    val totalSent: Int = 0,
    val maxRepeats: Int? = null,
    val contextData: Map<String, String>? = null
)

/**
 * Condición para mensajes condicionales
 */
data class MessageCondition(
    val field: String,
    val operator: String,
    val value: String
)

/**
 * Plantilla de mensaje
 */
data class MessageTemplate(
    val id: String,
    val type: ScheduledMessageType,
    val name: String,
    val template: String,
    val variables: List<String> = emptyList(),
    val isActive: Boolean = true
)

/**
 * Registro de mensaje enviado
 */
data class MessageLog(
    val messageId: String,
    val scheduledMessageId: String,
    val phone: String,
    val message: String,
    val sentAt: Long,
    val status: ScheduledMessageStatus,
    val error: String? = null
)

/**
 * Estadísticas del scheduler
 */
data class SchedulerStats(
    val totalScheduled: Int,
    val pending: Int,
    val sent: Int,
    val failed: Int,
    val cancelled: Int,
    val byType: Map<ScheduledMessageType, Int>
)

/**
 * Métricas de efectividad
 */
data class EffectivenessMetrics(
    val totalSent: Int,
    val responded: Int,
    val converted: Int,
    val responseRate: Double,
    val conversionRate: Double
)
