/*
 * SponsorFlow Nexus v2.4 - Offline Queue Entity
 * CORREGIDO: Version actualizada a v2.4
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
    
    val type: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val lastAttempt: Long? = null,
    val lastError: String? = null,
    val endpoint: String,
    val method: String = "POST",
    val headers: String? = null,
    val priority: Int = 2
)
