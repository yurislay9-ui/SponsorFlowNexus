/*
 * SponsorFlow Nexus v2.3 - Contact Entity
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
        Index(value = ["lastMessageAt"])
    ]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val lastMessageAt: Long = 0,
    val conversationCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)