/*
 * SponsorFlow Nexus v1.0 - App Error Types
 * Skill: Seguridad - Categorización de errores
 */
package com.sponsorflow.nexus.core.result

sealed class AppError {
    // Errores de red
    data class NetworkError(val message: String, val code: Int? = null) : AppError()
    data class TimeoutError(val message: String) : AppError()
    
    // Errores de base de datos
    data class DatabaseError(val cause: Throwable) : AppError()
    
    // Errores de autenticación y permisos
    data class AuthError(val message: String) : AppError()
    data class PermissionError(val message: String) : AppError()
    
    // Errores de seguridad
    data class SecurityError(val message: String) : AppError()
    data class ValidationError(val message: String, val field: String? = null) : AppError()
    
    // Errores de lógica de negocio
    data class LicenseError(val message: String, val code: Int? = null) : AppError()
    data class PaymentError(val message: String, val code: Int? = null) : AppError()
    data class SubscriptionError(val message: String) : AppError()
    
    // Errores de recursos
    data class ResourceNotFound(val resource: String, val id: String? = null) : AppError()
    data class ResourceExhausted(val resource: String) : AppError()
    
    // Errores de inventario
    data class NotFound(val message: String) : AppError()
    data class InsufficientStock(val message: String) : AppError()
    
    // Error desconocido
    data object Unknown : AppError()
    
    // Errores inesperados
    data class UnexpectedError(val cause: Throwable? = null) : AppError()
    
    // Errores de parsing
    data class ParseError(val message: String, val rawData: String? = null) : AppError()
    
    // Errores de IA
    data class AIError(val message: String, val code: String? = null) : AppError()

    companion object {
        fun fromException(e: Throwable): AppError = UnexpectedError(e)
    }
    
    fun toUserMessage(): String = when (this) {
        is NetworkError -> "Error de conexión. Verifica tu internet."
        is TimeoutError -> "Tiempo de espera agotado. Intenta de nuevo."
        is DatabaseError -> "Error de base de datos."
        is AuthError -> "Error de autenticación."
        is PermissionError -> "Permiso denegado."
        is SecurityError -> "Error de seguridad."
        is ValidationError -> message
        is LicenseError -> "Error de licencia: $message"
        is PaymentError -> "Error de pago: $message"
        is SubscriptionError -> "Error de suscripción: $message"
        is ResourceNotFound -> "Recurso no encontrado."
        is ResourceExhausted -> "Recurso agotado."
        is NotFound -> message
        is InsufficientStock -> message
        is Unknown -> "Error desconocido."
        is UnexpectedError -> "Error inesperado."
        is ParseError -> "Error al procesar datos."
    }
}
