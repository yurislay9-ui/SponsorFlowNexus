/*
 * SponsorFlow Nexus v2.3 - Offline Queue Entity
 */
package com.sponsorflow.nexus.offline

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "offline_queue",
    indices = [Index(value = ["type", "timestamp"])]
)
data class OfflineQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Tipo de operación: payment, webhook, license, etc.
    val type: String,
    
    // Datos en JSON
    val payload: String,
    
    // Timestamp de creación
    val timestamp: Long = System.currentTimeMillis(),
    
    // Intentos de envío
    val attempts: Int = 0,
    
    // Último intento
    val lastAttempt: Long? = null,
    
    // Error del último intento
    val lastError: String? = null,
    
    // Endpoint destino
    val endpoint: String,
    
    // Método HTTP
    val method: String = "POST",
    
    // Headers adicionales en JSON
    val headers: String? = null,
    
    // Prioridad (1=alta, 2=media, 3=baja)
    val priority: Int = 2
)