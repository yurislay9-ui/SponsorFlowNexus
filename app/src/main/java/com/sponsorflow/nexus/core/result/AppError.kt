/*
 * SponsorFlow Nexus v2.3 - App Error Types
 * Skill: Seguridad - Categorización de errores
 */
package com.sponsorflow.nexus.core.result

sealed class AppError {
    data class NetworkError(val code: Int? = null, val message: String? = null) : AppError()
    data class DatabaseError(val cause: Throwable? = null) : AppError()
    data class LicenseError(val reason: String) : AppError()
    data class AIError(val reason: String) : AppError()
    data class PluginError(val pluginId: String, val reason: String) : AppError()
    data class SecurityError(val reason: String) : AppError()
    data class ValidationError(val field: String, val reason: String) : AppError()
    data class PaymentError(val reason: String) : AppError()
    data class UnknownError(val cause: Throwable? = null) : AppError()

    fun toUserMessage(): String = when (this) {
        is NetworkError -> "Error de conexión"
        is DatabaseError -> "Error de datos"
        is LicenseError -> "Error de licencia: $reason"
        is AIError -> "Error de IA: $reason"
        is PluginError -> "Error en plugin: $reason"
        is SecurityError -> "Error de seguridad: $reason"
        is ValidationError -> "Dato inválido: $field"
        is PaymentError -> "Error de pago: $reason"
        is UnknownError -> "Error inesperado"
    }

    companion object {
        fun fromException(t: Throwable): AppError = when (t) {
            is java.net.UnknownHostException -> NetworkError(message = "Sin conexión")
            is java.net.SocketTimeoutException -> NetworkError(message = "Tiempo agotado")
            else -> UnknownError(cause = t)
        }
    }
}