/*
 * SponsorFlow Nexus v2.3 - Operation Status
 * Skill: Mejores prácticas - Estados definidos
 */
package com.sponsorflow.nexus.core.enums

enum class OperationStatus {
    IDLE,
    RUNNING,
    PAUSED,
    ERROR,
    SUSPENDED_WHATSAPP_UPDATE;

    fun canProcess(): Boolean = this == RUNNING
    fun canStart(): Boolean = this == IDLE || this == ERROR || this == PAUSED
    fun canPause(): Boolean = this == RUNNING
    fun canStop(): Boolean = this == RUNNING || this == PAUSED
    fun isProblematic(): Boolean = this == ERROR || this == SUSPENDED_WHATSAPP_UPDATE

    fun getDescription(): String = when (this) {
        IDLE -> "Servicio detenido"
        RUNNING -> "Procesando mensajes"
        PAUSED -> "Servicio pausado"
        ERROR -> "Error en el servicio"
        SUSPENDED_WHATSAPP_UPDATE -> "WhatsApp actualizado - actualización requerida"
    }
}