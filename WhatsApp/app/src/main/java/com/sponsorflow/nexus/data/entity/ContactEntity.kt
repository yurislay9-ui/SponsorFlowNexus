/*
 * SponsorFlow Nexus v1.0 - Contact Entity
 * Skill: Mejores prácticas - Room entity con índices
 */
package com.sponsorflow.nexus.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["lastMessageTime"])
    ]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String?,
    val phone: String,
    val lastMessage: String? = null,
    val lastMessageTime: Long = 0,
    val isBlocked: Boolean = false,
    val messageCount: Int = 0,
    val lastInteraction: Long = System.currentTimeMillis()
)
